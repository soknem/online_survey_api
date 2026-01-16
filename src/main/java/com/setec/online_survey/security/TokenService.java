package com.setec.online_survey.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtEncoder jwtEncoder;

    public void setTokensAsCookies(Authentication auth, HttpServletResponse response) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        String access = generateToken(user, roles, Duration.ofMinutes(15));
        String refresh = generateToken(user, null, Duration.ofDays(7));

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("access_token", access, 900));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("refresh_token", refresh, 604800));
    }

    public String generateToken(CustomUserDetails user, String roles, Duration duration) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("online_survey").issuedAt(Instant.now()).expiresAt(Instant.now().plus(duration))
                .subject(user.getUsername()).claim("uuid", user.getUuid())
                .claim("roles", roles).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String buildCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(true).path("/")
                .maxAge(maxAge).sameSite("Lax").build().toString();
    }
}
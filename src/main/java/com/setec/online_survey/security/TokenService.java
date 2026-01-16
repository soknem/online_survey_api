package com.setec.online_survey.security;

import com.setec.online_survey.features.auth.dto.TokenPair;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
        TokenPair tokens = generateTokenPair(auth);
        setTokensAsCookies(tokens, response);
    }

    public TokenPair generateTokenPair(Authentication auth) {
        Object principal = auth.getPrincipal();
        String username;
        String uuid = "";

        // Handle Google / OIDC User
        if (principal instanceof OidcUser oidcUser) {
            username = oidcUser.getEmail();
            // If your CustomOAuth2UserService adds the DB UUID to attributes, get it here
            uuid = oidcUser.getAttribute("uuid") != null ? oidcUser.getAttribute("uuid").toString() : "";
        }
        // Handle Standard DB User
        else if (principal instanceof CustomUserDetails userDetails) {
            username = userDetails.getUsername();
            uuid = userDetails.getUuid();
        }
        else {
            username = principal.toString();
        }

        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        String accessToken = generateToken(username, uuid, roles, Duration.ofMinutes(15));
        String refreshToken = generateToken(username, uuid, null, Duration.ofDays(3));

        return new TokenPair(accessToken, refreshToken);
    }

    public void setTokensAsCookies(TokenPair tokenPair, HttpServletResponse response) {
        // Access token (15 mins = 900s)
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("access_token", tokenPair.accessToken(), 900));
        // Refresh token (3 days = 259200s)
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("refresh_token", tokenPair.refreshToken(), 259200));
    }

    private String generateToken(String username, String uuid, String roles, Duration duration) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8080")
                .issuedAt(now)
                .expiresAt(now.plus(duration))
                .subject(username)
                .claim("uuid", uuid);

        if (roles != null) {
            claims.claim("roles", roles);
        }

        return jwtEncoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue();
    }

    private String buildCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // Set to true if using HTTPS
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build().toString();
    }
}
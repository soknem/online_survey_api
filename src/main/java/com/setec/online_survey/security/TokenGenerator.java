package com.setec.online_survey.security;

import com.setec.online_survey.features.auth.dto.AuthResponse;
import com.setec.online_survey.features.auth.dto.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class TokenGenerator {

    private final JwtEncoder jwtAccessTokenEncoder;
    private final @Qualifier("jwtRefreshTokenEncoder") JwtEncoder jwtRefreshTokenEncoder;

    private String createAccessToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("SA_ONLINE_SURVEY")
                .issuedAt(now)
                // --- MODIFIED: Reduced to 15 minutes for security ---
                .expiresAt(now.plus(1, ChronoUnit.MINUTES))
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getRoles())
                .build();
        return jwtAccessTokenEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String createRefreshToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("SA_ONLINE_SURVEY")
                .issuedAt(now)
                // --- MODIFIED: Set to 3 days ---
                .expiresAt(now.plus(2, ChronoUnit.MINUTES))
                .subject(userDetails.getUsername())
                .build();
        return jwtRefreshTokenEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public TokenPair generateTokens(Authentication authentication) { // <--- MODIFIED RETURN TYPE
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = createAccessToken(userDetails);

        String refreshToken;
        if (authentication.getCredentials() instanceof Jwt jwt && isRefreshTokenReusable(jwt)) {
            refreshToken = jwt.getTokenValue();
        } else {
            refreshToken = createRefreshToken(userDetails);
        }

        // Return the internal TokenPair
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private boolean isRefreshTokenReusable(Jwt jwt) {
        Instant now = Instant.now();
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return false;
        // Reuse if refresh token has >= 24 hours left (adjust logic as needed)
        return Duration.between(now, expiresAt).toHours() >= 24;
    }
}
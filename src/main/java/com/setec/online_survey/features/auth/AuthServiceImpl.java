package com.setec.online_survey.features.auth;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.UserRole;
import com.setec.online_survey.features.auth.dto.*;
import com.setec.online_survey.features.send_mail.SendMailService;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.security.CustomUserDetails;
import com.setec.online_survey.security.TokenGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // <--- NEW IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie; // <--- NEW IMPORT
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DaoAuthenticationProvider authenticationProvider;
    private final TokenGenerator tokenGenerator;
    private final JwtDecoder jwtRefreshTokenDecoder;
    private final SendMailService sendMailService;

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Stream.of(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // Helper method to set cookies
    @Override
    public void setTokensAsCookies(TokenPair tokenPair, HttpServletResponse response) {
        // Access Token Cookie (15 min)
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", tokenPair.accessToken()) // <--- FIXED
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        // Refresh Token Cookie (3 days)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken()) // <--- FIXED
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh")
                .maxAge(259200)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    @Transactional
    @Override
    public void register(RegisterRequest request) {
        // ... (existing register logic remains the same) ...
        if (userRepository.existsByEmailAndEmailVerifiedTrue(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if(!request.password().equals(request.confirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm password miss match");
        }

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        user.setIsDeleted(false);
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(false);
        user.setIsCredentialsNonExpired(true);
        user.setEmailVerified(false);

        user.setRole(UserRole.ROLE_USER);

        userRepository.save(user);

        sendMailService.generateRegisterOtp(user);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                request.email(),
                request.password()
        );
        Authentication  authenticated = authenticationProvider.authenticate(authRequest);

        // MODIFIED: Returns TokenPair, not AuthResponse
        TokenPair tokens = tokenGenerator.generateTokens(authenticated);

        // Set tokens as HttpOnly cookies
        setTokensAsCookies(tokens, response);

        // Return the public DTO without tokens in the body
        return AuthResponse.builder().message("Login successful").build();
    }

    // Refresh token logic – called from controller
    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) { // <--- MODIFIED

        // --- STEP 1: Get Refresh Token from the HTTP-Only Cookie ---
        String refreshTokenValue = getCookieValue(request, "refresh_token");

        if (refreshTokenValue == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token cookie not found");
        }

        TokenPair tokens = null;
        try {
            // --- STEP 2: Decode and validate the token value ---
            Jwt jwt = jwtRefreshTokenDecoder.decode(refreshTokenValue);

            // Load full user from DB using subject (email)
            CustomUserDetails userDetails = (CustomUserDetails) userRepository
                    .findUserByEmail(jwt.getSubject())
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create authentication object with Jwt as credentials (for token rotation)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    jwt, // Pass the old RT so TokenGenerator can check if it's reusable
                    userDetails.getAuthorities()
            );

            // --- STEP 3: Generate new tokens ---
            tokens = tokenGenerator.generateTokens(authentication);

            // --- STEP 4: Set new tokens as HttpOnly cookies ---
            setTokensAsCookies(tokens, response);

            return AuthResponse.builder().message("Token refreshed").build();

        } catch (Exception e) {
            // Handle expired, invalid, or malformed JWTs
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }
}
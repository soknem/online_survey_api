package com.setec.online_survey.features.auth;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.UserRole;
import com.setec.online_survey.features.auth.dto.*;
import com.setec.online_survey.features.mail.EmailVerificationTokenService;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.security.CustomUserDetails;
import com.setec.online_survey.security.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DaoAuthenticationProvider authenticationProvider;
    private final TokenGenerator tokenGenerator;
    private final JwtDecoder jwtRefreshTokenDecoder;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Transactional
    @Override
    public void register(RegisterRequest request) {

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
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(false);
        user.setCredentialsNonExpired(true);
        user.setEmailVerified(false);

        user.setRole(UserRole.ROLE_USER);

        userRepository.save(user);

        emailVerificationTokenService.generate(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                request.email(),
                request.password()
        );
        Authentication  authenticated = authenticationProvider.authenticate(authRequest);
        return tokenGenerator.generateTokens(authenticated);
    }

    // Refresh token logic – called from controller
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            // Decode and validate refresh token using the refresh-token decoder
            Jwt jwt = jwtRefreshTokenDecoder.decode(request.refreshToken());

            // Load full user from DB using subject (email)
            CustomUserDetails userDetails = (CustomUserDetails) userRepository
                    .findUserByEmail(jwt.getSubject())
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create authentication object with Jwt as credentials (for token rotation)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    jwt,  // important: pass the old refresh token so TokenGenerator can reuse it
                    userDetails.getAuthorities()
            );

            return tokenGenerator.generateTokens(authentication);

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
    }
}
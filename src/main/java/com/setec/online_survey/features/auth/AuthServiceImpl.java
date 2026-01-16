package com.setec.online_survey.features.auth;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.UserRole;
import com.setec.online_survey.features.auth.dto.*;
import com.setec.online_survey.features.send_mail.SendMailService;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.security.CustomUserDetails;
import com.setec.online_survey.security.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // <--- NEW IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie; // <--- NEW IMPORT
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService; // Use the new consolidated service
    private final JwtDecoder jwtDecoder;
    private final SendMailService sendMailService;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authenticated = authenticationManager.authenticate(authRequest);

        TokenPair tokens = tokenService.generateTokenPair(authenticated);
        tokenService.setTokensAsCookies(tokens, response);

        return AuthResponse.builder().message("Login successful").build();
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = getCookieValue(request, "refresh_token");
        if (refreshTokenValue == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        try {
            Jwt jwt = jwtDecoder.decode(refreshTokenValue);
            CustomUserDetails userDetails = (CustomUserDetails) userRepository.findUserByEmail(jwt.getSubject())
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // ROTATION: Generate a brand new pair (New Access + New Refresh)
            TokenPair newTokens = tokenService.generateTokenPair(auth);
            tokenService.setTokensAsCookies(newTokens, response);

            return AuthResponse.builder().message("Token rotated successfully").build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }


    @Transactional
    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmailAndEmailVerifiedTrue(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if(!request.password().equals(request.confirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm password miss match");
        }

        User user = userRepository.findByEmailAndEmailVerifiedFalseAndIsAccountNonLockedFalse(request.email()).orElse(new User());

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

}
package com.setec.online_survey.features.auth;

import com.setec.online_survey.features.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    // Helper method to set cookies
    void setTokensAsCookies(TokenPair tokenPair, HttpServletResponse response);

    @Transactional
    void register(RegisterRequest request);

    // MODIFIED: Pass HttpServletResponse to set cookies
    AuthResponse login(LoginRequest request, HttpServletResponse response);

    // MODIFIED: Pass HttpServletResponse to set cookies
    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);
}

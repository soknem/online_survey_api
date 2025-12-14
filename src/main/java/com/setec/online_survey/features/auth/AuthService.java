package com.setec.online_survey.features.auth;

import com.setec.online_survey.features.auth.dto.AuthResponse;
import com.setec.online_survey.features.auth.dto.LoginRequest;
import com.setec.online_survey.features.auth.dto.RefreshTokenRequest;
import com.setec.online_survey.features.auth.dto.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional
    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}

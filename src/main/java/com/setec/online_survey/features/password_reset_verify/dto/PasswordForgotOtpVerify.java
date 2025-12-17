package com.setec.online_survey.features.password_reset_verify.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordForgotOtpVerify(
        @NotBlank(message = "Username is required")
        String email,

        @NotBlank(message = "Token is required")
        String token
) {
}

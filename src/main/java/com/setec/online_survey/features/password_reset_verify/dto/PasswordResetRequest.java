package com.setec.online_survey.features.password_reset_verify.dto;

public record PasswordResetRequest(

        String newPassword,
        String confirmPassword,
        String email,
        String token
) {
}

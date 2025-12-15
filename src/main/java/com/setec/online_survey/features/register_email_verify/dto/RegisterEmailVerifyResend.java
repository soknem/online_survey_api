package com.setec.online_survey.features.register_email_verify.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterEmailVerifyResend(
        @NotBlank(message = "Username is required")
        String email
) {
}
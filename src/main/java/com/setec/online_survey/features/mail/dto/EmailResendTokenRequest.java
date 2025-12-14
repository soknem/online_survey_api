package com.setec.online_survey.features.mail.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailResendTokenRequest(
        @NotBlank(message = "Username is required")
        String email
) {
}
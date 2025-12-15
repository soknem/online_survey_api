package com.setec.online_survey.features.register_email_verify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RegisterEmailVerifyRequest(

        @NotBlank(message = "Username is required")
        String email,

        @NotBlank(message = "Token is required")
        String token

) {
}
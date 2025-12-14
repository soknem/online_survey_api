package com.setec.online_survey.features.mail.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EmailVerifyRequest(

        @NotBlank(message = "Username is required")
        String email,

        @NotBlank(message = "Token is required")
        String token

) {
}
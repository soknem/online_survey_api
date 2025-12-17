package com.setec.online_survey.features.password_reset_verify.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public record PasswordForgotRequest (
        @NotBlank(message = "Username is required")
        String email
){
}

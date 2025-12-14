package com.setec.online_survey.features.auth.dto;

import jakarta.persistence.Column;

public record RegisterRequest(
        String email,
        String password,
        String confirmPassword,
        String dateOfBirth,
        String firstName,
        String lastName
) { }
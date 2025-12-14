package com.setec.online_survey.features.auth.dto;

import lombok.Builder;

// Internal DTO used only within the backend to transfer tokens from Generator to Service
@Builder
public record TokenPair(
        String accessToken,
        String refreshToken
) { }
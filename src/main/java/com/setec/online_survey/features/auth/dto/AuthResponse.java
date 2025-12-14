package com.setec.online_survey.features.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String message
) { }
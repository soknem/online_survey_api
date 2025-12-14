package com.setec.online_survey.features.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public record UserProfileResponse(
        String email,
        String roles
) {
}

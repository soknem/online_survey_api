package com.setec.online_survey.features.user.dto;

import java.time.LocalDate;

public record UserResponse(

        String uuid,
        String email,

        LocalDate dateOfBirth,
        String firstName,
        String lastName
) {
}

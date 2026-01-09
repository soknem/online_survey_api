package com.setec.online_survey.features.survey.dto;

import com.setec.online_survey.domain.SurveyType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record SurveyRequest(
        @NotBlank
        String title,
        String description,

        String image

) {

}

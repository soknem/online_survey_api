package com.setec.online_survey.features.survey.dto;

import com.setec.online_survey.domain.SurveyType;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

public record SurveyRequest(
        String title,
        String description,

        String startDate,

        String closeDate,

        Boolean isPublic,

        Boolean isClosed,

        SurveyType surveyType

) {

}

package com.setec.online_survey.features.survey.dto;

import com.setec.online_survey.domain.SurveyType;

import java.time.LocalDateTime;

public record SurveyResponse(
        String uuid,
        String title,
        String description,

        String startDate,

        String closeDate,

        Boolean isPublic,

        Boolean isClosed,

        SurveyType surveyType

){

}

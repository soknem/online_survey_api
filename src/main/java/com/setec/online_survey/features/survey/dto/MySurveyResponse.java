package com.setec.online_survey.features.survey.dto;

import com.setec.online_survey.domain.SurveyType;

public record MySurveyResponse(

        String uuid,
        String title,
        String description,

        String startDate,

        String closeDate,

        Boolean isPublic,

        Boolean isClosed,

        SurveyType surveyType
) {
}

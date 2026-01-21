package com.setec.online_survey.features.survey.dto;

import com.setec.online_survey.domain.SurveyType;
import com.setec.online_survey.features.question.dto.QuestionResponse;

import java.util.List;

public record SurveyPublicResponse(

        String uuid,
        String title,
        String description,

        String startDate,

        String closeDate,

        SurveyType surveyType,

        List<QuestionResponse> questions
) {
}

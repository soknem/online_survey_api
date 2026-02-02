package com.setec.online_survey.features.reponse.dto;

import com.setec.online_survey.domain.SurveyType;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionRequest(
        LocalDateTime startTime,
        String surveyUuid,
        List<SubmissionAnswerRequest> answers,
        String fingerprint,
        String browserUuid,
        String userAgent
) {
}

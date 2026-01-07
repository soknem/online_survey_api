package com.setec.online_survey.features.reponse.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionRequest(
         LocalDateTime startTime,
         String surveyUuid,
         List<SubmissionAnswerRequest> answers
) {
}

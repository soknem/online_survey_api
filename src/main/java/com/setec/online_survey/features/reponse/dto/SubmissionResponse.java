package com.setec.online_survey.features.reponse.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionResponse(

         String uuid,
         String title,
         String description,

         LocalDateTime startTime,

         List<SubmissionAnswerResponse> answers
) {
}

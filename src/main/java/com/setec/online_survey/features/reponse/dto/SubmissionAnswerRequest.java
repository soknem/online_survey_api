package com.setec.online_survey.features.reponse.dto;

import java.util.List;

public record SubmissionAnswerRequest(
         String questionUuid,
         List<String> optionUuid,
         String answerText
) {
}

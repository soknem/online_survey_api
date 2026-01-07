package com.setec.online_survey.features.reponse.dto;

import com.setec.online_survey.domain.QuestionType;
import com.setec.online_survey.features.option.dto.OptionResponse;

import java.util.List;

public record SubmissionAnswerResponse(
        String uuid,
        String questionText,
        QuestionType questionType,
        Integer orderIndex,
        Boolean isRequired,
        List<OptionResponse> options
) {
}

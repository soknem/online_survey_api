package com.setec.online_survey.features.question.dto;

import com.setec.online_survey.domain.QuestionType;
import com.setec.online_survey.features.option.dto.OptionRequest;

import java.util.List;

public record QuestionRequest(

        String uuid,
        String questionText,
        QuestionType questionType,
        Integer orderIndex,
        Boolean isRequired,
        List<OptionRequest> options
) {
}

package com.setec.online_survey.features.option.dto;

import com.setec.online_survey.domain.QuestionType;

public record OptionRequest(

        Integer orderIndex,
        String optionText
) {
}

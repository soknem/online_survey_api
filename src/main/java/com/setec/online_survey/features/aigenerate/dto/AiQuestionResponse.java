package com.setec.online_survey.features.aigenerate.dto;

import com.setec.online_survey.domain.QuestionType;

import java.util.List;

public record AiQuestionResponse(
    String questionText,
    QuestionType questionType,
    Integer orderIndex,
    Boolean isRequired,
    List<AiOptionResponse> options
) {}
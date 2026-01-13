package com.setec.online_survey.features.aigenerate.dto;

public record AiGenerateRequest(
    String prompt,
    String surveyTitle,
    String surveyType,
    int numberOfQuestions,
    String level,

    String provider
) {}
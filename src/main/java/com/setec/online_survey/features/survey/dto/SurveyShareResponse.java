package com.setec.online_survey.features.survey.dto;

public record SurveyShareResponse(
        String link,
        String qrCodeUrl
) {
}

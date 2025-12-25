package com.setec.online_survey.features.share.dto;

public record ShareResponse(

        String shareLink,
        String qrCodeFileName,
        String qrCodeUrl
) {
}

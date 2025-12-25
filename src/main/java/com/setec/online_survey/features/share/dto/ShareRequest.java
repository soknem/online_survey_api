package com.setec.online_survey.features.share.dto;

import jakarta.validation.constraints.NotBlank;

public record ShareRequest(
        @NotBlank(message = "shareAlias cannot be blank")
        String shareAlias
) {
}

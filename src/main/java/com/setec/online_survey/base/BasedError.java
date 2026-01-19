package com.setec.online_survey.base;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasedError<T> {
    private String code;

    // Detail error for user
    private T description;

}


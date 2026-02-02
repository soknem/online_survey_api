package com.setec.online_survey.features.reponse;

import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.reponse.dto.SubmissionResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ResponseService {

    SubmissionResponse getSubmissionByShareSlug(String shareSlug);

    void submitSurvey(SubmissionRequest submissionRequest , Authentication authentication);
}

package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.SurveyType;
import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.reponse.dto.SubmissionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ResponseService {

    SubmissionResponse getSubmissionByShareSlug(String shareSlug);

    void submitSurvey(SubmissionRequest submissionRequest , Authentication authentication);

   void validatePersonBeAbleToSurvey(String surveyUuid,boolean isAuth,String userUuid, String fingerPrint, String browserUuid);

    String getClientIp(HttpServletRequest request);
}

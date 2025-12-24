package com.setec.online_survey.features.survey;

import com.setec.online_survey.features.survey.dto.SurveyRequest;
import com.setec.online_survey.features.survey.dto.SurveyResponse;
import com.setec.online_survey.features.survey.dto.SurveyShareResponse;

import java.util.List;

public interface SurveyService {

    void createSurvey(SurveyRequest surveyRequest);

    SurveyResponse getSurveyByUuid(String uuid);

    List<SurveyResponse> getAllSurvey();

    void deleteSurveyByUuid(String uuid);

    SurveyResponse updateSurveyByUuid(SurveyRequest surveyRequest,String uuid);

     void surveyPublicStatus(String uuid);

     SurveyResponse getPublicSurveyByLink(String link);

     SurveyShareResponse shareSurvey(String surveyUui);


}

package com.setec.online_survey.features.survey;

import com.setec.online_survey.features.survey.dto.*;

import java.util.List;

public interface SurveyService {

    void createSurvey(SurveyRequest surveyRequest);

    SurveyResponse getSurveyByUuid(String uuid);

    List<SurveyResponse> getAllSurvey();

    void deleteSurveyByUuid(String uuid);

    SurveyResponse updateSurveyByUuid(SurveyRequest surveyRequest,String uuid);

     void surveyPublicStatus(String uuid);

     SurveyShareResponse shareSurvey(SurveyShareRequest shareRequest);

     SurveyPublicResponse getShareSurvey(String slug);


}

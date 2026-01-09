package com.setec.online_survey.features.survey;

import com.setec.online_survey.base.BaseSpecification;
import com.setec.online_survey.features.survey.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.WebRequest;

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

     Page<MySurveyResponse> getMySurvey(BaseSpecification.FilterDto filterBody, WebRequest request, String globalOperator, String sortBy, Sort.Direction orderBy, int pageNumber, int pageSize, Authentication authentication);




}

package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import org.springframework.core.io.Resource;

import java.util.List;

public interface AiGenerateService {


    List<AiQuestionResponse> generateSurvey(AiGenerateRequest request);

    List<AiQuestionResponse> generateSurveyMultimodal(
            AiGenerateRequest request,
            Resource fileResource,
            String mimeType);
}
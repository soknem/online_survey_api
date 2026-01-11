package com.setec.online_survey.features.aigenerate;

import com.setec.online_survey.features.aigenerate.dto.*;
import java.util.List;

public interface AiGenerateService {


    List<AiQuestionResponse> generateSurvey(AiGenerateRequest request);
}
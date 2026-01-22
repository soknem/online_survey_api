package com.setec.online_survey.features.question;

import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;

import java.util.List;

public interface QuestionService {

    List<QuestionResponse> updateSurveyQuestions(List<QuestionRequest> questionRequest,String surveyUuid);

    QuestionResponse getQuestionByUuid(String uuid);

    List<QuestionResponse> getAllQuestion();

    List<QuestionResponse> getQuestionBySurveyUuid(String surveyUuid);

    QuestionResponse updateQuestionByUuid(QuestionRequest questionRequest,String questionUuid);

    void deleteQuestionByUuid(String questionUuid);
}

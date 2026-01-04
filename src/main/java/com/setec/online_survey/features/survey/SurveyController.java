package com.setec.online_survey.features.survey;


import com.setec.online_survey.features.question.QuestionService;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import com.setec.online_survey.features.survey.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final QuestionService questionService;

    @PostMapping
    public void createSurvey(@RequestBody SurveyRequest surveyRequest){
        surveyService.createSurvey(surveyRequest);
    }

    @GetMapping("/{uuid}")
    public SurveyResponse getSurveyByUuid(@PathVariable String uuid){
        return surveyService.getSurveyByUuid(uuid);
    }

    @GetMapping()
    public List<SurveyResponse> getAllSurveys(){
        return surveyService.getAllSurvey();
    }

    @DeleteMapping
    public void deleteSurveyByUuid(String uuid){
        surveyService.deleteSurveyByUuid(uuid);
    }

    @PutMapping("/{uuid}/status")
    public void surveyPublicStatus(@PathVariable String uuid){
        surveyService.surveyPublicStatus(uuid);
    }

    @PostMapping("/share")
    public SurveyShareResponse shareSurvey(@RequestBody SurveyShareRequest surveyShareRequest){
        return surveyService.shareSurvey(surveyShareRequest);
    }

    @GetMapping("/share/{slug}")
    public SurveyPublicResponse getPublicShareSurvey(@PathVariable String slug){
       return   surveyService.getShareSurvey(slug);
    }


    @PatchMapping("/{surveyUuid}/question")
    public void editSurveyQuestion(@RequestBody List<QuestionRequest> questionRequests, @PathVariable String surveyUuid){
        questionService.updateSurveyQuestions(questionRequests,surveyUuid);
    }

    @GetMapping("/{uuid}/question")
    public List<QuestionResponse> getQuestionBySurvey(@PathVariable String uuid){
      return  questionService.getQuestionBySurveyUuid(uuid);
    }

}

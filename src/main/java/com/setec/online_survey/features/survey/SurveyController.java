package com.setec.online_survey.features.survey;


import com.setec.online_survey.features.survey.dto.SurveyRequest;
import com.setec.online_survey.features.survey.dto.SurveyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

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

    @GetMapping("/share/{alias}")
    public SurveyResponse getPublicSurvey(@PathVariable String alias){
        return surveyService.getPublicSurveyByLink(alias);
    }

}

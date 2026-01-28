package com.setec.online_survey.features.survey;


import com.setec.online_survey.base.BaseSpecification;
import com.setec.online_survey.features.question.QuestionService;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import com.setec.online_survey.features.survey.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSurvey(@RequestBody @Valid SurveyRequest surveyRequest) {
        surveyService.createSurvey(surveyRequest);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('USER')")
    public SurveyResponse getSurveyByUuid(@PathVariable String uuid) {
        return surveyService.getSurveyByUuid(uuid);
    }

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public List<SurveyResponse> getAllSurveys() {
        return surveyService.getAllSurvey();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public void deleteSurveyByUuid(String uuid) {
        surveyService.deleteSurveyByUuid(uuid);
    }

    @PutMapping("/{uuid}/status")
    @PreAuthorize("hasRole('USER')")
    public void surveyPublicStatus(@PathVariable String uuid) {
        surveyService.surveyPublicStatus(uuid);
    }

    @PostMapping("/share")
    @PreAuthorize("hasRole('USER')")
    public SurveyShareResponse shareSurvey(@RequestBody SurveyShareRequest surveyShareRequest,
                                           @RequestParam(defaultValue = "prod") String stg,
                                           @RequestParam(defaultValue = "false") String isNew
    ) {
        return surveyService.shareSurvey(surveyShareRequest,stg,isNew);
    }

    @GetMapping("/share/{slug}")
    //@PreAuthorize("hasRole('USER')")
    public SurveyPublicResponse getPublicShareSurvey(@PathVariable String slug) {
        return surveyService.getShareSurvey(slug);
    }


    @PatchMapping("/{surveyUuid}/question")
    @PreAuthorize("hasRole('USER')")
    public List<QuestionResponse> editSurveyQuestion(@RequestBody List<QuestionRequest> questionRequests, @PathVariable String surveyUuid) {
       return questionService.updateSurveyQuestions(questionRequests, surveyUuid);
    }

    @GetMapping("/{uuid}/question")
    @PreAuthorize("hasRole('USER')")
    public List<QuestionResponse> getQuestionBySurvey(@PathVariable String uuid) {
        return questionService.getQuestionBySurveyUuid(uuid);
    }

    @PostMapping ("/my-survey")
    @PreAuthorize("hasRole('USER')")
//@PreAuthorize("hasAnyAuthority('faculty:read')")
    public Page<MySurveyResponse> getMySurvey(
            WebRequest request,
            @RequestParam(value = "gop", defaultValue = "AND") String globalOperator,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(value = "orderBy", defaultValue = "ASC") Sort.Direction orderBy,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "25") int pageSize,
            @RequestBody(required = false) BaseSpecification.FilterDto filterBody,
            Authentication authentication
    ) {

        return surveyService.getMySurvey(filterBody,request, globalOperator, sortBy,orderBy,pageNumber, pageSize,authentication);
    }

}
package com.setec.online_survey.features.reponse;

import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.survey.SurveyService;
import com.setec.online_survey.features.survey.dto.SurveyPublicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/responses")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;
    private final SurveyService surveyService;

    @PostMapping("/submit")
    public void submitSurvey(@RequestBody SubmissionRequest submissionRequest, Authentication authentication){
        responseService.submitSurvey(submissionRequest,authentication);
    }

    @PostMapping("/share/{slug}")
    //@PreAuthorize("hasRole('USER')")
    public SurveyPublicResponse getPublicShareSurvey(@PathVariable String slug, @RequestBody SubmissionRequest submissionRequest, Authentication authentication) {
        return surveyService.getShareSurvey(slug,submissionRequest,authentication);
    }
}

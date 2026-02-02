package com.setec.online_survey.features.reponse;

import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/responses")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;

    @PostMapping("/submit")
    public void submitSurvey(@RequestBody SubmissionRequest submissionRequest, Authentication authentication){
        responseService.submitSurvey(submissionRequest,authentication);
    }
}

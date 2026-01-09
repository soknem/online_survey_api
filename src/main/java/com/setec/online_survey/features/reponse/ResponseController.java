package com.setec.online_survey.features.reponse;

import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/surveys/submit")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;

    @PostMapping
    public void submitSurvey(@RequestBody SubmissionRequest submissionRequest){
        responseService.submitSurvey(submissionRequest);
    }
}

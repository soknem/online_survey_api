package com.setec.online_survey.features.question;


import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionResponse> getAllQuestion(){
        return questionService.getAllQuestion();
    }

    @DeleteMapping("/{uuid}")
    public void deleteQuestion(@PathVariable String uuid){
        questionService.deleteQuestionByUuid(uuid);
    }
}

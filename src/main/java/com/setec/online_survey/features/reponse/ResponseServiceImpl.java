package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.Question;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.features.option.OptionRepository;
import com.setec.online_survey.features.question.QuestionRepository;
import com.setec.online_survey.features.reponse.dto.SubmissionAnswerResponse;
import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.reponse.dto.SubmissionResponse;
import com.setec.online_survey.features.survey.SurveyRepository;
import com.setec.online_survey.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponseServiceImpl implements ResponseService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ResponseRepository repository;
    private final QuestionMapper  questionMapper;

    @Override
    public SubmissionResponse getSubmissionByShareSlug(String shareSlug) {

        Survey survey = surveyRepository.findSurveyBySurveyUrl(shareSlug).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey = %s  has not been found", shareSlug))
        );

        if (!survey.getIsPublic() || survey.getIsClosed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("survey = %s has been closed or private", shareSlug));
        }

        List<SubmissionAnswerResponse> question = questionRepository.findQuestionBySurveyUuid(survey.getUuid()).stream().map(questionMapper::toSubmissionAnswerResponse).toList();

        return new SubmissionResponse(
                survey.getUuid(),
                survey.getTitle(),
                survey.getDescription(),
                LocalDateTime.now(),
                question
        );
    }

    @Override
    public void submitSurvey(SubmissionRequest submissionRequest) {

    }
}

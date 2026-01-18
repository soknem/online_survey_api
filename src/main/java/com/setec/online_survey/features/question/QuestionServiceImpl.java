package com.setec.online_survey.features.question;

import com.setec.online_survey.domain.Option;
import com.setec.online_survey.domain.Question;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import com.setec.online_survey.features.survey.SurveyRepository;
import com.setec.online_survey.features.survey.dto.SurveyResponse;
import com.setec.online_survey.mapper.OptionMapper;
import com.setec.online_survey.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService{

    private final SurveyRepository surveyRepository;

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final OptionMapper optionMapper;

    @Override
    @Transactional
    public void updateSurveyQuestions(List<QuestionRequest> questionRequests, String surveyUuid) {


        // 1. Find the parent Survey
        Survey survey = surveyRepository.findSurveyByUuid(surveyUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("survey = %s has not been found", surveyUuid)));

        // 2. Map and Update Questions
        Set<Question> updatedQuestions = questionRequests.stream()
                .map(request -> {
                    Question question = questionRepository.findQuestionByUuid(request.uuid())
                            .orElseGet(() -> {
                                Question newQ = questionMapper.fromQuestionRequest(request);
                                newQ.setUuid(UUID.randomUUID().toString());
                                return newQ;
                            });

                    // Update basic fields via mapper
                    questionMapper.updateQuestion(question, request);
                    question.setSurvey(survey);

                    // 3. Handle Options (Orphan Removal for Options)
                    var existingOptions = question.getOptions();
                    existingOptions.clear(); // Clears old options from DB due to orphanRemoval=true

                    if (request.options() != null) {
                        request.options().forEach(optRequest -> {

                            Option option = optionMapper.fromOptionRequest(optRequest);

                            if (option.getUuid() == null) {
                                option.setUuid(UUID.randomUUID().toString());
                            }
                            question.addOption(option); // Crucial: sets option.setQuestion(question)
                        });
                    }
                    return question;
                })
                .collect(Collectors.toSet());

        // 4. SYNC Questions with Survey (Orphan Removal for Questions)
        // First, clear the existing collection to trigger orphan removal for deleted questions
        survey.getQuestions().clear();
        // Then add the updated/new set
        survey.getQuestions().addAll(updatedQuestions);

        // 5. Save the Parent (Survey)
        // Because of CascadeType.ALL, saving the survey saves all questions and options
        surveyRepository.save(survey);
    }

    @Override
    public QuestionResponse getQuestionByUuid(String uuid) {

        Question question = questionRepository.findQuestionByUuid(uuid).
                orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("question = %s has not been found",uuid)));

        return questionMapper.toQuestionResponse(question);
    }

    @Override
    public List<QuestionResponse> getAllQuestion() {
        return questionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Question::getOrderIndex,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(questionMapper::toQuestionResponse)
                .toList();
    }

    @Override
    public List<QuestionResponse> getQuestionBySurveyUuid(String surveyUuid) {
        return questionRepository.findQuestionBySurveyUuid(surveyUuid).stream().map(questionMapper::toQuestionResponse).toList();
    }

    @Override
    public QuestionResponse updateQuestionByUuid(QuestionRequest questionRequest,String questionUuid) {

        Question question = questionRepository.findQuestionByUuid(questionUuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("question = %s has not been found",questionUuid)));

        questionMapper.updateQuestion(question,questionRequest);

        questionRepository.save(question);

        return  questionMapper.toQuestionResponse(question);

    }

    @Override
    public void deleteQuestionByUuid(String questionUuid) {

        Question question = questionRepository.findQuestionByUuid(questionUuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("question = %s has not been found",questionUuid)));

        questionRepository.delete(question);
    }
}

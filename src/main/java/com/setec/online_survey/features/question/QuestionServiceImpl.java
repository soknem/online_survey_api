package com.setec.online_survey.features.question;

import com.setec.online_survey.domain.Option;
import com.setec.online_survey.domain.Question;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.features.option.dto.OptionRequest;
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
    public List<QuestionResponse>  updateSurveyQuestions(List<QuestionRequest> questionRequests, String surveyUuid) {
        Survey survey = surveyRepository.findSurveyByUuid(surveyUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found"));

        // 1. Map existing questions for lookup
        Map<String, Question> existingQuestionMap = survey.getQuestions().stream()
                .collect(Collectors.toMap(Question::getUuid, q -> q));

        Set<Question> finalQuestions = new HashSet<>();

        for (QuestionRequest request : questionRequests) {
            Question question;

            if (request.uuid() != null) {
                // If UUID is provided, it MUST exist in this specific survey
                if (!existingQuestionMap.containsKey(request.uuid())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid Question UUID: " + request.uuid() + ". This question does not belong to the survey or does not exist.");
                }
                question = existingQuestionMap.get(request.uuid());
                questionMapper.updateQuestion(question, request);
            } else {
                // Truly new question
                question = questionMapper.fromQuestionRequest(request);
                question.setUuid(UUID.randomUUID().toString());
                question.setSurvey(survey);
            }

            // 2. Handle Options with same validation logic
            updateOptions(question, request.options());

            finalQuestions.add(question);
        }

        // 3. Sync and Save
        survey.getQuestions().retainAll(finalQuestions);
        survey.getQuestions().addAll(finalQuestions);

        surveyRepository.save(survey);

        return getQuestionBySurveyUuid(surveyUuid);
    }

    private void updateOptions(Question question, List<OptionRequest> optionRequests) {
        // If request has no options, clear existing ones
        if (optionRequests == null || optionRequests.isEmpty()) {
            if (question.getOptions() != null) {
                question.getOptions().clear();
            }
            return;
        }

        // Map existing options for this specific question
        Map<String, Option> existingOptionMap = (question.getOptions() == null) ? Map.of() :
                question.getOptions().stream()
                        .filter(o -> o.getUuid() != null)
                        .collect(Collectors.toMap(Option::getUuid, o -> o));

        Set<Option> finalOptions = new HashSet<>();

        for (OptionRequest optReq : optionRequests) {
            Option option;
            if (optReq.uuid() != null) {
                // If UUID is provided, it MUST exist in this specific question
                if (!existingOptionMap.containsKey(optReq.uuid())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid Option UUID: " + optReq.uuid() + ". This option does not belong to the question or does not exist.");
                }
                option = existingOptionMap.get(optReq.uuid());
                optionMapper.updateOption(option, optReq);
            } else {
                // Truly new option
                option = optionMapper.fromOptionRequest(optReq);
                option.setUuid(UUID.randomUUID().toString());
                option.setQuestion(question); // Explicitly set the back-reference for Oracle
            }
            finalOptions.add(option);
        }

        // Hibernate orphanRemoval sync
        question.getOptions().retainAll(finalOptions);
        question.getOptions().addAll(finalOptions);
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
        return questionRepository.findQuestionBySurveyUuid(surveyUuid)
                .stream()
                .map(questionMapper::toQuestionResponse)
                .toList();
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

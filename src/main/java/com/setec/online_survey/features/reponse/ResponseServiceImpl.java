package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.*;
import com.setec.online_survey.features.option.OptionRepository;
import com.setec.online_survey.features.question.QuestionRepository;
import com.setec.online_survey.features.reponse.dto.SubmissionAnswerResponse;
import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.reponse.dto.SubmissionResponse;
import com.setec.online_survey.features.survey.SurveyRepository;
import com.setec.online_survey.mapper.QuestionMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponseServiceImpl implements ResponseService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ResponseRepository repository;
    private final QuestionMapper  questionMapper;
    private final ResponseRepository sessionRepository;
    private final HttpServletRequest request;

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
        // 1. Get Client IP (for the ip_address column in your schema)
        String clientIp = getClientIp(request);

        // 2. Validate "One Person Per Survey" constraint
        validateOnePersonPerSurvey(submissionRequest, clientIp);

        // 3. Find Survey
        Survey survey = surveyRepository.findSurveyByUuid(submissionRequest.surveyUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Survey = %s has not been found",submissionRequest.surveyUuid())));

        // 4. Create and Populate Session
        ResponseSession session = new ResponseSession();
        session.setSurvey(survey);
        session.setStartTime(submissionRequest.startTime());
        session.setSubmitTime(LocalDateTime.now());
        session.setIpAddress(clientIp);
        session.setFingerprint(submissionRequest.fingerprint());
        session.setBrowserUuid(submissionRequest.browserUuid());
        session.setUserAgent(request.getHeader("User-Agent"));

        // Note: respondent_id remains null for anonymous users

        // 5. Map Answers
//        Set<Answer> answers = submissionRequest.answers().stream().map(ansDto -> {
//            Answer answer = new Answer();
//            answer.setSession(session);
//            answer.setQuestion(questionRepository.findQuestionByUuid(ansDto.questionUuid())
//                    .orElseThrow(() -> new EntityNotFoundException("Question not found")));

            // Handle multiple options (Checkbox) or single option (Radio)
//            if (ansDto.optionUuid() != null && !ansDto.optionUuid().isEmpty()) {
//                Set<Option> options = new HashSet<>(optionRepository.findAllByUuidIn(ansDto.optionUuid()));
//                answer.setOption(options);
          //  }

           // answer.setAnswerText(ansDto.answerText());
        //    return answer;
      //  }).collect(Collectors.toSet());

       // session.setAnswers(answers);

        // 6. Save (CascadeType.ALL will save answers automatically)
        sessionRepository.save(session);
    }

    private void validateOnePersonPerSurvey(SubmissionRequest request, String ip) {
        // Check Hardware Fingerprint
        if (sessionRepository.existsBySurveyUuidAndFingerprint(request.surveyUuid(), request.fingerprint())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Multiple submissions detected (Device Fingerprint)");
        }

        // Check LocalStorage UUID
        if (sessionRepository.existsBySurveyUuidAndBrowserUuid(request.surveyUuid(), request.browserUuid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Multiple submissions detected (Browser UUID)");
        }

        // Check IP Address (Existing constraint in your schema)
//        if (sessionRepository.existsBySurveyUuidAndIpAddress(request.surveyUuid(), ip)) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Multiple submissions detected (IP Address)");
//        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can be a comma-separated list; the first one is the real client
        return xfHeader.split(",")[0].trim();
    }
}

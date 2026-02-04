package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.*;
import com.setec.online_survey.features.option.OptionRepository;
import com.setec.online_survey.features.question.QuestionRepository;
import com.setec.online_survey.features.reponse.dto.SubmissionAnswerResponse;
import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.reponse.dto.SubmissionResponse;
import com.setec.online_survey.features.survey.SurveyRepository;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.mapper.QuestionMapper;
import com.setec.online_survey.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    private final QuestionMapper questionMapper;
    private final ResponseRepository sessionRepository;
    private final HttpServletRequest request;
    private final UserRepository userRepository;

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
    public void submitSurvey(SubmissionRequest submissionRequest, Authentication authentication) {

        // 1. Find Survey
        Survey survey = surveyRepository.findSurveyByUuid(submissionRequest.surveyUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Survey = %s has not been found", submissionRequest.surveyUuid())));

        SurveyType surveyType = survey.getSurveyType();
        User user = null;
        boolean isAuth= surveyType != SurveyType.ANONYMOUS;

        if (isAuth) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User unauthorized");
            }

            user = userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(userDetails.getUsername()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User unauthorized"));
        }

        // 2. Get Client IP (for the ip_address column in your schema)
        String clientIp = getClientIp(request);

        // 3. Validate "One Person Per Survey" constraint
        this.validatePersonBeAbleToSurvey(survey.getUuid(),isAuth, isAuth?user.getUuid():null, submissionRequest.fingerprint(), submissionRequest.browserUuid() );

        // 4. Create and Populate Session
        ResponseSession session = new ResponseSession();
        session.setSurvey(survey);
        session.setRespondent(user);
        session.setStartTime(submissionRequest.startTime());
        session.setSubmitTime(LocalDateTime.now());
        session.setIpAddress(clientIp);
        session.setFingerprint(submissionRequest.fingerprint());
        session.setBrowserUuid(submissionRequest.browserUuid());
        session.setUserAgent(request.getHeader("User-Agent"));


        // 5. Map Answers
        Set<Answer> answers = submissionRequest.answers().stream().map(ansDto -> {
            Answer answer = new Answer();
            answer.setSession(session);
            answer.setQuestion(questionRepository.findQuestionByUuid(ansDto.questionUuid())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found")));

         //Handle multiple options (Checkbox) or single option (Radio)
            if (ansDto.optionUuid() != null && !ansDto.optionUuid().isEmpty()) {
                Set<Option> options = new HashSet<>(optionRepository.findAllByUuidIn(ansDto.optionUuid()));
                answer.setOption(options);
          }

         answer.setAnswerText(ansDto.answerText());
            return answer;
          }).collect(Collectors.toSet());

         session.setAnswers(answers);

        // 6. Save (CascadeType.ALL will save answers automatically)
        sessionRepository.save(session);
    }

    @Override
    public void validatePersonBeAbleToSurvey(String surveyUuid,boolean isAuth, String userUuid, String fingerPrint, String browserUuid) {

        boolean check = sessionRepository.existsBySurveyLogic(surveyUuid, isAuth, userUuid, fingerPrint, browserUuid);

        if (!check) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "this survey already submitted");
        }
    }

    @Override
    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can be a comma-separated list; the first one is the real client
        return xfHeader.split(",")[0].trim();
    }
}

package com.setec.online_survey.features.survey;

import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.survey.dto.SurveyRequest;
import com.setec.online_survey.features.survey.dto.SurveyResponse;
import com.setec.online_survey.features.survey.dto.SurveyShareResponse;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.mapper.SurveyMapper;
import com.setec.online_survey.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService{

    private final SurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final UserRepository userRepository;

    @Override
    public void createSurvey(SurveyRequest surveyRequest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User creator ;

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User unauthorized");
        }

        creator=userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(user.getUsername()).orElseThrow(
                ()-> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("user = %s has not been found",user.getUsername()))
        );

        Survey survey = surveyMapper.fromSurveyRequest(surveyRequest);

        survey.setUuid(UUID.randomUUID().toString());
        survey.setCreator(creator);
        survey.setSurveyUrl(UUID.randomUUID().toString());

        surveyRepository.save(survey);
    }

    @Override
    public SurveyResponse getSurveyByUuid(String uuid) {

        Survey survey =surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey = %s has not been found",uuid)));

        return surveyMapper.toSurveyResponse(survey);
    }

    @Override
    public List<SurveyResponse> getAllSurvey() {
        return surveyRepository.findAll().stream().map(surveyMapper::toSurveyResponse).toList();
    }

    @Override
    public void deleteSurveyByUuid(String uuid) {

        Survey survey =surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey = %s has not been found",uuid)));

        surveyRepository.delete(survey);
    }

    @Override
    public SurveyResponse updateSurveyByUuid(SurveyRequest surveyRequest,String uuid) {


        Survey survey =surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey = %s has not been found",uuid)));

        surveyMapper.updateSurvey(survey,surveyRequest);
        surveyRepository.save(survey);

        return surveyMapper.toSurveyResponse(survey);
    }

    @Override
    public void surveyPublicStatus(String uuid) {

        Survey survey =surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey = %s has not been found",uuid)));

        survey.setIsPublic(!survey.getIsPublic());

        surveyRepository.save(survey);

    }

    @Override
    public SurveyResponse getPublicSurveyByLink(String link) {

        Survey survey =surveyRepository.findSurveyBySurveyUrl(link)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("surveyUrl = %s has not been found",link)));

        if(!survey.getIsPublic()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey not found"));
        }
        if(survey.getIsClosed()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("survey has been closed"));
        }

        return surveyMapper.toSurveyResponse(survey);
    }

    @Override
    public SurveyShareResponse shareSurvey(String surveyUui) {
        return null;
    }
}

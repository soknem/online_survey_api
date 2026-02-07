package com.setec.online_survey.features.survey;

import com.github.slugify.Slugify;
import com.setec.online_survey.base.BaseSpecification;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.domain.SurveyType;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.qr_generate.QrGenerateService;
import com.setec.online_survey.features.qr_generate.dto.QrCodeRequest;
import com.setec.online_survey.features.qr_generate.dto.QrCodeResponse;
import com.setec.online_survey.features.qr_generate.dto.QrGenerateResponse;
import com.setec.online_survey.features.question.QuestionService;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import com.setec.online_survey.features.qr_generate.dto.ShareRequest;
import com.setec.online_survey.features.reponse.ResponseService;
import com.setec.online_survey.features.reponse.dto.SubmissionRequest;
import com.setec.online_survey.features.survey.dto.*;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.mapper.SurveyMapper;
import com.setec.online_survey.security.CustomUserDetails;
import com.setec.online_survey.util.SortUtils;
import com.setec.online_survey.util.SpecUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final UserRepository userRepository;
    private final QrGenerateService qrGenerateService;
    private final BaseSpecification<Survey> baseSpecification;
    private final QuestionService questionService;
    private final ResponseService responseService;

    //endpoint that handle manage medias
    @Value("${media.survey-share}")
    private String surveyShare;

    @Value("${media.survey-share-prod}")
    private String surveyShareProd;

    @Value("${media.base-uri}")
    private String baseUri;

    //endpoint that handle manage medias
    @Value("${media.image-end-point}")
    private String imageEndpoint;

    @Override
    public void createSurvey(SurveyRequest surveyRequest) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User creator;

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User unauthorized");
        }

        creator = userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(user.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("user = %s has not been found", user.getUsername()))
        );

        Survey survey = surveyMapper.fromSurveyRequest(surveyRequest);

        survey.setUuid(UUID.randomUUID().toString());
        survey.setCreator(creator);

        surveyRepository.save(survey);
    }

    @Override
    public SurveyResponse getSurveyByUuid(String uuid) {

        Survey survey = surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey = %s has not been found", uuid)));

        return surveyMapper.toSurveyResponse(survey);
    }

    @Override
    public List<SurveyResponse> getAllSurvey() {
        return surveyRepository.findAll().stream().map(surveyMapper::toSurveyResponse).toList();
    }

    @Override
    public void deleteSurveyByUuid(String uuid) {

        Survey survey = surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey = %s has not been found", uuid)));

        surveyRepository.delete(survey);
    }

    @Override
    public SurveyResponse updateSurveyByUuid(SurveyRequest surveyRequest, String uuid) {


        Survey survey = surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey = %s has not been found", uuid)));

        surveyMapper.updateSurvey(survey, surveyRequest);
        surveyRepository.save(survey);

        return surveyMapper.toSurveyResponse(survey);
    }

    @Override
    public void surveyPublicStatus(String uuid) {

        Survey survey = surveyRepository.findSurveyByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey = %s has not been found", uuid)));

        survey.setIsPublic(!survey.getIsPublic());

        surveyRepository.save(survey);

    }


    @Override
    public SurveyShareResponse shareSurvey(SurveyShareRequest surveyShareRequest,String stg,String isNew) {

        Survey survey = surveyRepository.findSurveyByUuid(surveyShareRequest.surveyUuid()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Survey =%s has not been found", surveyShareRequest.surveyUuid()))
        );

        if (survey.getIsClosed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey has been closed");
        }

        if (!survey.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey not yet public");
        }


        String linkEndpoint;
        String qrCodeUrl;
        String shareLink;

        if(survey.getSurveyUrl() !=null && survey.getQrCodeUrl()!=null&&isNew.equalsIgnoreCase("false")){
            linkEndpoint=survey.getSurveyUrl();
            shareLink= (Objects.equals(stg, "prod") ? surveyShareProd :surveyShare)+linkEndpoint;
            qrCodeUrl=survey.getQrCodeUrl();

        }else{

            final Slugify slg = Slugify.builder().build();
            String uuid = UUID.randomUUID().toString();
            linkEndpoint = slg.slugify(survey.getTitle()) + uuid;

            shareLink= (Objects.equals(stg, "prod") ? surveyShareProd :surveyShare)+linkEndpoint;

            QrCodeResponse qrGenerateResponse= qrGenerateService.generateAndUploadQRCode(new QrCodeRequest(shareLink));

            qrCodeUrl = qrGenerateResponse.url();

            survey.setSurveyUrl(linkEndpoint);
            survey.setQrCodeUrl(qrCodeUrl);
        }

        surveyRepository.save(survey);

        return new SurveyShareResponse(shareLink, qrCodeUrl);
    }

    @Override
    public SurveyPublicResponse getShareSurvey(String slug, SubmissionRequest submissionRequest,Authentication authentication) {

        Survey survey = surveyRepository.findSurveyBySurveyUrl(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("surveyUrl = %s has not been found", slug)));

        if (!survey.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey not found"));
        }
        if (survey.getIsClosed()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey has been closed"));
        }

        User user = null;
        boolean isAuth= survey.getSurveyType() != SurveyType.ANONYMOUS;

        if (isAuth) {
            user = Optional.ofNullable(authentication)
                    .map(auth -> (CustomUserDetails) auth.getPrincipal())
                    .flatMap(details -> userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(details.getUsername()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User unauthorized"));
        }

        // 3. Validate "One Person Per Survey" constraint
        responseService.validatePersonBeAbleToSurvey(survey.getUuid(),isAuth, isAuth?user.getUuid():null, submissionRequest.fingerprint(), submissionRequest.browserUuid() );

        List<QuestionResponse> questions = questionService.getQuestionBySurveyUuid(survey.getUuid());

        return surveyMapper.toSurveyPublicResponse(survey,questions);
    }

    @Override
    public Page<MySurveyResponse> getMySurvey(BaseSpecification.FilterDto filterBody, WebRequest request, String globalOperator, String sortBy,Sort.Direction orderBy, int pageNumber, int pageSize, Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User unauthorized");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        assert userDetails != null;
        String email = userDetails.getUsername();


        SortUtils.validateSortBy(Survey.class, sortBy);

        // 2. Define Mandatory Conditions (Ownership)
        Map<String, Object> moreSpec = Map.ofEntries(
                Map.entry("creator.email", userDetails.getUsername())
        );

        // 3. Use the Util to get the final specification
        Specification<Survey> finalSpec = SpecUtils.buildFinalSpec(
                baseSpecification,
                filterBody,
                request,
                globalOperator,
                moreSpec
        );

        if (pageSize == 0) {
            pageSize = (int) surveyRepository.count(finalSpec);
        }

        //create sort order
        Sort sortById = Sort.by(orderBy, sortBy);

        //create pagination with current pageNumber and pageSize of pageNumber
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sortById);

        //find all subject in database
        Page<Survey> surveys = surveyRepository.findAll(finalSpec, pageRequest);

        //map entity to DTO and return
        return surveys.map(survey -> {

            String thumb=null;
            if (survey.getImage() != null) {
                 thumb = baseUri + imageEndpoint + "/view/" + survey.getImage();
            }

            return surveyMapper.toMySurveyResponse(survey, thumb);
        });
    }
}

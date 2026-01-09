package com.setec.online_survey.features.survey;

import com.github.slugify.Slugify;
import com.setec.online_survey.base.BaseSpecification;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.auth.dto.UserProfileResponse;
import com.setec.online_survey.features.share.ShareService;
import com.setec.online_survey.features.share.dto.ShareRequest;
import com.setec.online_survey.features.share.dto.ShareResponse;
import com.setec.online_survey.features.survey.dto.*;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.mapper.SurveyMapper;
import com.setec.online_survey.security.CustomUserDetails;
import com.setec.online_survey.util.FilterUtils;
import com.setec.online_survey.util.SortUtils;
import com.setec.online_survey.util.SpecUtils;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final UserRepository userRepository;
    private final ShareService shareService;
    private final BaseSpecification<Survey> baseSpecification;

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
        survey.setSurveyUrl(UUID.randomUUID().toString());

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
    public SurveyShareResponse shareSurvey(SurveyShareRequest surveyShareRequest) {

        Survey survey = surveyRepository.findSurveyByUuid(surveyShareRequest.surveyUuid()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Survey =%s has not been found", surveyShareRequest.surveyUuid()))
        );

        if (survey.getIsClosed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey has been closed");
        }

        if (!survey.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Survey not yet public");
        }

        final Slugify slg = Slugify.builder().build();
        String uuid = UUID.randomUUID().toString();
        String linkEndpoint = slg.slugify(survey.getTitle()) + uuid;

        ShareResponse shareResponse = shareService.shareSurvey(new ShareRequest(linkEndpoint));

        String qrCodeFileName = shareResponse.qrCodeFileName();
        String qrCodeUrl = shareResponse.qrCodeUrl();
        String shareLink = shareResponse.shareLink();

        survey.setSurveyUrl(linkEndpoint);
        survey.setQrCodeUrl(qrCodeFileName);

        surveyRepository.save(survey);

        return new SurveyShareResponse(shareLink, qrCodeUrl);
    }

    @Override
    public SurveyPublicResponse getShareSurvey(String slug) {

        Survey survey = surveyRepository.findSurveyBySurveyUrl(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("surveyUrl = %s has not been found", slug)));

        if (!survey.getIsPublic()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey not found"));
        }
        if (survey.getIsClosed()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("survey has been closed"));
        }

        return surveyMapper.toSurveyPublicResponse(survey);
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

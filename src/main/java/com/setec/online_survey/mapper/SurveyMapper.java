package com.setec.online_survey.mapper;

import com.setec.online_survey.domain.Option;
import com.setec.online_survey.domain.Question;
import com.setec.online_survey.domain.Survey;
import com.setec.online_survey.features.option.dto.OptionRequest;
import com.setec.online_survey.features.option.dto.OptionResponse;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.survey.dto.MySurveyResponse;
import com.setec.online_survey.features.survey.dto.SurveyPublicResponse;
import com.setec.online_survey.features.survey.dto.SurveyRequest;
import com.setec.online_survey.features.survey.dto.SurveyResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SurveyMapper {

    Survey fromSurveyRequest(SurveyRequest surveyRequest);

    SurveyResponse toSurveyResponse(Survey survey);

    SurveyPublicResponse toSurveyPublicResponse(Survey survey);

    MySurveyResponse toMySurveyResponse(Survey survey,Integer totalResponse, String thumbnail);

    @Mapping(target = "totalResponse", expression = "java(survey.getResponseSessions() != null ? survey.getResponseSessions().size() : 0)")
    @Mapping(target = "thumbnail", source = "thumbnail")
    MySurveyResponse toMySurveyResponse(Survey survey, String thumbnail);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSurvey(@MappingTarget Survey survey, SurveyRequest surveyRequest);
}

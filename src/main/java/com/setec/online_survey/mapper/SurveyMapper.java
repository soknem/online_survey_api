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
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SurveyMapper {
    Survey fromSurveyRequest(SurveyRequest surveyRequest);

    SurveyResponse toSurveyResponse(Survey survey);

    SurveyPublicResponse toSurveyPublicResponse(Survey survey);

    MySurveyResponse toMySurveyResponse(Survey survey);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSurvey(@MappingTarget Survey survey, SurveyRequest surveyRequest);
}

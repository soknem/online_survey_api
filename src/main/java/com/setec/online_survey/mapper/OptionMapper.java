package com.setec.online_survey.mapper;

import com.setec.online_survey.domain.Option;
import com.setec.online_survey.domain.Question;
import com.setec.online_survey.features.option.dto.OptionRequest;
import com.setec.online_survey.features.option.dto.OptionResponse;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OptionMapper {
    Option fromOptionRequest(OptionRequest optionRequest);

    OptionResponse toOptionResponse(Option option);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "question", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOption(@MappingTarget Option option, OptionRequest optionRequest);
}

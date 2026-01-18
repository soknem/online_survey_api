package com.setec.online_survey.mapper;

import com.setec.online_survey.domain.Option;
import com.setec.online_survey.domain.Question;
import com.setec.online_survey.features.option.dto.OptionRequest;
import com.setec.online_survey.features.option.dto.OptionResponse;
import com.setec.online_survey.features.question.dto.QuestionRequest;
import com.setec.online_survey.features.question.dto.QuestionResponse;
import com.setec.online_survey.features.reponse.dto.SubmissionAnswerResponse;
import org.mapstruct.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    Question fromQuestionRequest(QuestionRequest questionRequest);

    @Mapping(target = "options", expression = "java(sortOptions(question.getOptions()))")
    QuestionResponse toQuestionResponse(Question question);

    // Changed parameter type from List → Set
    default List<OptionResponse> sortOptions(Set<Option> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }

        return options.stream()
                .sorted(Comparator.comparingInt(Option::getOrderIndex))
                .map(this::toOptionResponse)
                .toList();
    }

    OptionResponse toOptionResponse(Option option);

    SubmissionAnswerResponse toSubmissionAnswerResponse(Question question);

    @Mapping(target = "options", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuestion(@MappingTarget Question question, QuestionRequest questionRequest);
}
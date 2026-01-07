package com.setec.online_survey.features.survey;

import com.setec.online_survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey,Long>, JpaSpecificationExecutor<Survey> {

    Optional<Survey> findSurveyByUuid(String uuid);

    Optional<Survey> findSurveyBySurveyUrl(String url);
}

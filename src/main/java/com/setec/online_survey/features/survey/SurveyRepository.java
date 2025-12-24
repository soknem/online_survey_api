package com.setec.online_survey.features.survey;

import com.setec.online_survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey,Long> {

    Optional<Survey> findSurveyByUuid(String uuid);

    Optional<Survey> findSurveyBySurveyUrl(String url);
}

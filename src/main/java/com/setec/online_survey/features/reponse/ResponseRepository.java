package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.ResponseSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<ResponseSession,Long> {

    boolean existsBySurveyUuidAndFingerprint(String surveyUuid, String fingerprint);
    boolean existsBySurveyUuidAndBrowserUuid(String surveyUuid, String browserUuid);
    boolean existsBySurveyUuidAndIpAddress(String surveyUuid, String ipAddress);
}

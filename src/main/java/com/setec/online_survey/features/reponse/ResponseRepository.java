package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.ResponseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResponseRepository extends JpaRepository<ResponseSession,Long> {

    @Query("""
    SELECT COUNT(rs) > 0 
    FROM ResponseSession rs 
    WHERE rs.survey.uuid = :surveyUuid 
    AND (
        (:isAuthType = true AND rs.respondent.id = :userUuid)
        OR 
        (:isAuthType = false AND (rs.fingerprint = :fingerprint OR rs.browserUuid = :browserUuid))
    )
""")
    boolean existsBySurveyLogic(
            @Param("surveyUuid") String surveyUuid,
            @Param("isAuthType") boolean isAuthType,
            @Param("userUuid") String userUuid,
            @Param("fingerprint") String fingerprint,
            @Param("browserUuid") String browserUuid
    );
}

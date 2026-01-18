package com.setec.online_survey.features.question;

import com.setec.online_survey.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question,Long> {

    Optional<Question> findQuestionByUuid(String uuid);

//    List<Question> findQuestionBySurveyUuid(String uuid);

    // In QuestionRepository
    @Query("SELECT q FROM Question q " +
            "LEFT JOIN FETCH q.options o " +           // if you need options eagerly loaded
            "WHERE q.survey.uuid = :surveyUuid " +
            "ORDER BY q.orderIndex ASC")
    List<Question> findQuestionBySurveyUuid(@Param("surveyUuid") String surveyUuid);
}

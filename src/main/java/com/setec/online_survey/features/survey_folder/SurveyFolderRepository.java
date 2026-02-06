package com.setec.online_survey.features.survey_folder;

import com.setec.online_survey.domain.SurveyFolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyFolderRepository extends JpaRepository<SurveyFolder,Long> {
}

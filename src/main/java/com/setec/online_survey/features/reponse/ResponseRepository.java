package com.setec.online_survey.features.reponse;

import com.setec.online_survey.domain.ResponseSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<ResponseSession,Long> {
}

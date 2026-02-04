package com.setec.online_survey.features.option;

import com.setec.online_survey.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option,Long> {

    List<Option> findOptionByQuestionUuid(String uuid);

    Optional<Option> findOptionByUuid(String uuid);

    List<Option> findAllByUuidIn(List<String> uuidList);
}

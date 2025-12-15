package com.setec.online_survey.features.mail;



import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> getByToken(String token);

    Optional<VerificationToken> findByUser(User user);
    @Transactional
    @Modifying
    @Query("DELETE FROM VerificationToken e where e.user=:user")
    void deleteByUser(@Param("user") User user);
}
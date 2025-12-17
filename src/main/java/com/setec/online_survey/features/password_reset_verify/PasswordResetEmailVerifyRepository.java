package com.setec.online_survey.features.password_reset_verify;

import com.setec.online_survey.domain.PasswordResetToken;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetEmailVerifyRepository extends JpaRepository<PasswordResetToken,Long> {

    Optional<PasswordResetToken> findByUser(User user);

    Boolean existsByUserEmail(String email);

    void deleteByUser(User user);

    void deleteByUserEmail(String email);
}

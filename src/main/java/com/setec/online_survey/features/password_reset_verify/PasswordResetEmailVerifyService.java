package com.setec.online_survey.features.password_reset_verify;

import com.setec.online_survey.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetEmailVerifyService extends JpaRepository<PasswordResetToken,Long> {
}

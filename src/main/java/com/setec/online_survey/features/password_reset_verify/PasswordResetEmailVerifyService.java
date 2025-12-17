package com.setec.online_survey.features.password_reset_verify;

import com.setec.online_survey.domain.PasswordResetToken;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotOtpVerify;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotRequest;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordResetRequest;

public interface PasswordResetEmailVerifyService {

    void forgotPasswordRequest(PasswordForgotRequest passwordForgotRequest);

    boolean isUsersToken(PasswordResetToken token, User user);

    boolean isExpired(PasswordResetToken token);

    void resend(PasswordForgotRequest passwordForgotRequest);

    User verify(PasswordForgotOtpVerify passwordForgotOtpVerify);

    void resetPassword(PasswordResetRequest passwordResetRequest);
}

package com.setec.online_survey.features.register_email_verify;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import com.setec.online_survey.features.register_email_verify.dto.RegisterEmailVerifyRequest;

public interface RegisterEmailVerifyService {

    void verify(RegisterEmailVerifyRequest registerEmailVerifyRequest);

    boolean isUsersToken(VerificationToken token, User user);

    boolean isExpired(VerificationToken token);

    void resend(String username);

}
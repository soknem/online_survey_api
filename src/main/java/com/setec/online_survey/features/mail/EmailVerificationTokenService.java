package com.setec.online_survey.features.mail;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import com.setec.online_survey.features.mail.dto.EmailVerifyRequest;

public interface EmailVerificationTokenService {

    void verify(EmailVerifyRequest emailVerifyRequest);

    boolean isUsersToken(VerificationToken token, User user);

    void generate(User user);

    void sendMail(String to, String subject, String content);

    boolean isExpired(VerificationToken token);

    void resend(String username);

}
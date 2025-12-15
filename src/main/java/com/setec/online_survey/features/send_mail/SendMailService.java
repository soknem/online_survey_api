package com.setec.online_survey.features.send_mail;

import com.setec.online_survey.domain.User;

public interface SendMailService {

    void generateRegisterOtp(User user);

    void generateResetPasswordOtp(User user);
}

package com.setec.online_survey.features.register_email_verify;


import com.setec.online_survey.base.BasedMessage;

import com.setec.online_survey.features.register_email_verify.dto.RegisterEmailVerifyRequest;
import com.setec.online_survey.features.register_email_verify.dto.RegisterEmailVerifyResend;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email-verification")
@RequiredArgsConstructor
public class RegisterEmailVerifyController {

    private final RegisterEmailVerifyService registerEmailVerifyService;

    @PostMapping
    BasedMessage verify(@Valid @RequestBody RegisterEmailVerifyRequest registerEmailVerifyRequest) {
        registerEmailVerifyService.verify(registerEmailVerifyRequest);
        return new BasedMessage("Email has been verified successfully");
    }


    @PostMapping("/token")
    BasedMessage resendToken(@Valid @RequestBody RegisterEmailVerifyResend registerEmailVerifyResend) {
        registerEmailVerifyService.resend(registerEmailVerifyResend.email());
        return new BasedMessage("New confirmation link has been sent, check your emails");
    }

}
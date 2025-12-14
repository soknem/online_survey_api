package com.setec.online_survey.features.mail;


import com.setec.online_survey.base.BasedMessage;
import com.setec.online_survey.features.mail.dto.EmailResendTokenRequest;
import com.setec.online_survey.features.mail.dto.EmailVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email-verification")
@RequiredArgsConstructor
public class EmailVerificationTokenController {

    private final EmailVerificationTokenService emailVerificationTokenService;

    @PostMapping
    BasedMessage verify(@Valid @RequestBody EmailVerifyRequest emailVerifyRequest) {
        emailVerificationTokenService.verify(emailVerifyRequest);
        return new BasedMessage("Email has been verified successfully");
    }


    @PostMapping("/token")
    BasedMessage resendToken(@Valid @RequestBody EmailResendTokenRequest emailResendTokenRequest) {
        emailVerificationTokenService.resend(emailResendTokenRequest.email());
        return new BasedMessage("New confirmation link has been sent, check your emails");
    }

}
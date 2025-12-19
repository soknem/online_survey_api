package com.setec.online_survey.features.password_reset_verify;


import com.setec.online_survey.base.BasedMessage;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotOtpVerify;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotRequest;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordResetRequest;
import com.setec.online_survey.features.register_email_verify.dto.RegisterEmailVerifyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordResetEmailVerifyController {

    private final PasswordResetEmailVerifyService passwordResetEmailVerifyService;
    @PostMapping("/forgot")
    BasedMessage forgot(@Valid @RequestBody PasswordForgotRequest passwordForgotRequest) {
        passwordResetEmailVerifyService.forgotPasswordRequest(passwordForgotRequest);
        return new BasedMessage("Email has been verified successfully");
    }

    @PostMapping("/token")
    BasedMessage resend(@Valid @RequestBody PasswordForgotRequest passwordForgotRequest) {
        passwordResetEmailVerifyService.forgotPasswordRequest(passwordForgotRequest);
        return new BasedMessage("Email has been verified successfully");
    }

    @PostMapping("/verify")
    BasedMessage verify(@Valid @RequestBody PasswordForgotOtpVerify passwordForgotOtpVerify) {
        passwordResetEmailVerifyService.verify(passwordForgotOtpVerify);
        return new BasedMessage("Email has been verified successfully");
    }

    @PostMapping("/reset")
    BasedMessage reset(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        passwordResetEmailVerifyService.resetPassword(passwordResetRequest);
        return new BasedMessage("Email has been verified successfully");
    }
}

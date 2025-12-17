package com.setec.online_survey.features.password_reset_verify;

import com.setec.online_survey.domain.PasswordResetToken;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotOtpVerify;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordForgotRequest;
import com.setec.online_survey.features.password_reset_verify.dto.PasswordResetRequest;
import com.setec.online_survey.features.send_mail.SendMailService;
import com.setec.online_survey.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PasswordResetEmailVerifyServiceImpl implements PasswordResetEmailVerifyService{

    private final UserRepository userRepository;
    private final SendMailService sendMailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailVerifyRepository passwordResetEmailVerifyRepository;

    @Override
    public void forgotPasswordRequest(PasswordForgotRequest passwordForgotRequest) {
        User user = userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(passwordForgotRequest.email())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("User =%s has not been found",passwordForgotRequest.email())));
        boolean check =passwordResetEmailVerifyRepository.existsByUserEmail(user.getEmail());
        if(check){
            System.out.println("SOKNEM_VV");
            passwordResetEmailVerifyRepository.deleteByUser(user);

        }

        sendMailService.generateResetPasswordOtp(user);
    }

    @Override
    public boolean isUsersToken(PasswordResetToken token, User user) {
        return Objects.equals(user.getId(), token.getUser().getId());
    }

    @Override
    public boolean isExpired(PasswordResetToken token) {
        return !token.getExpiration().isBefore(LocalTime.now());
    }

    @Override
    public void resend(PasswordForgotRequest passwordForgotRequest) {
        User user = userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(passwordForgotRequest.email())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("User =%s has not been found",passwordForgotRequest.email())));

        passwordResetEmailVerifyRepository.deleteByUser(user);
        sendMailService.generateResetPasswordOtp(user);
    }

    @Override
    public User verify(PasswordForgotOtpVerify passwordForgotOtpVerify) {
        // 1. Find the user attempting to verify (by email)
        User foundUser = userRepository.findUserByEmailAndEmailVerifiedTrueAndIsAccountNonLockedTrue(passwordForgotOtpVerify.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with corresponding verification token"));

        PasswordResetToken foundToken = passwordResetEmailVerifyRepository.findByUser(foundUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification token is invalid"));

        // 3. COMPARE: Use passwordEncoder.matches() to verify the plain token against the stored hash
        boolean tokenMatches = passwordEncoder.matches(
                passwordForgotOtpVerify.token(),
                foundToken.getToken()
        );

        if (!tokenMatches) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification token is invalid");
        }

        // 4. Check expiration and complete verification
        if (this.isExpired(foundToken)) {
            LocalTime expiration = LocalTime.now().plusMinutes(1);
            foundToken.setExpiration(expiration);
            passwordResetEmailVerifyRepository.save(foundToken);
        }else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }
        return foundUser ;
    }

    @Override
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        User user=  verify(new PasswordForgotOtpVerify(passwordResetRequest.email(),passwordResetRequest.token()));

      if(!passwordResetRequest.confirmPassword().equals(passwordResetRequest.confirmPassword())){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"confirm password miss match");
      }
      user.setPassword(passwordEncoder.encode(passwordResetRequest.newPassword()));
      userRepository.save(user);
      passwordResetEmailVerifyRepository.deleteByUser(user);

    }
}

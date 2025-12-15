package com.setec.online_survey.features.register_email_verify;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import com.setec.online_survey.features.register_email_verify.dto.RegisterEmailVerifyRequest;
import com.setec.online_survey.features.send_mail.SendMailService;
import com.setec.online_survey.features.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterEmailVerifyServiceImpl implements RegisterEmailVerifyService {

    private final RegisterEmailVerifyRepository registerEmailVerifyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendMailService sendMailService;

    @Override
    public void verify(RegisterEmailVerifyRequest registerEmailVerifyRequest) {

        // 1. Find the user attempting to verify (by email)
        User foundUser = userRepository.findUserByEmailAndEmailVerifiedFalseAndIsAccountNonLockedFalse(registerEmailVerifyRequest.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with corresponding verification token"));

        VerificationToken foundToken = registerEmailVerifyRepository.findByUser(foundUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification token is invalid"));

        // 3. COMPARE: Use passwordEncoder.matches() to verify the plain token against the stored hash
        boolean tokenMatches = passwordEncoder.matches(
                registerEmailVerifyRequest.token(),
                foundToken.getToken()
        );

        if (!tokenMatches) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification token is invalid");
        }

        // 4. Check expiration and complete verification
        if (this.isExpired(foundToken)) {
            foundUser.setEmailVerified(true);
            foundUser.setIsAccountNonLocked(true);
            userRepository.save(foundUser);
            registerEmailVerifyRepository.deleteByUser(foundUser);
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
    }

    @Override
    public void resend(String username) {

        // check if user attempts to verify exists or not
        User foundUser = userRepository.findUserByEmailAndEmailVerifiedFalseAndIsAccountNonLockedFalse(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsuccessfully creation of confirmation link!"));

        registerEmailVerifyRepository.deleteByUser(foundUser);
        sendMailService.generateRegisterOtp(foundUser);
    }

    @Override
    public boolean isUsersToken(VerificationToken token, User user) {
        return Objects.equals(user.getId(), token.getUser().getId());
    }


    @Override
    public boolean isExpired(VerificationToken token) {

        return !token.getExpiration().isBefore(LocalTime.now());
    }

}
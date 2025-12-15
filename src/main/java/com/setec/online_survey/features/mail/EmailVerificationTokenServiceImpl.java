package com.setec.online_survey.features.mail;

import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import com.setec.online_survey.features.mail.dto.EmailVerifyRequest;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationTokenServiceImpl implements EmailVerificationTokenService {

    private final VerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void verify(EmailVerifyRequest emailVerifyRequest) {

        // check if user attempts to verify exists or not
        User foundUser = userRepository.findUserByEmailAndEmailVerifiedFalseAndIsAccountNonLockedFalse(emailVerifyRequest.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with corresponding verification token"));

        VerificationToken foundToken = emailVerificationTokenRepository.getByToken(emailVerifyRequest.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification token is invalid"));

        if (this.isUsersToken(foundToken, foundUser)) {
            if (this.isExpired(foundToken)) {
                foundUser.setEmailVerified(true);
                foundUser.setIsAccountNonLocked(true);
                userRepository.save(foundUser);
                emailVerificationTokenRepository.deleteByUser(foundUser);
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
    }

    @Override
    public void resend(String username) {

        // check if user attempts to verify exists or not
        User foundUser = userRepository.findUserByEmailAndEmailVerifiedFalseAndIsAccountNonLockedFalse(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsuccessfully creation of confirmation link!"));

        emailVerificationTokenRepository.deleteByUser(foundUser);
        generate(foundUser);
    }

    @Override
    public boolean isUsersToken(VerificationToken token, User user) {
        return Objects.equals(user.getId(), token.getUser().getId());
    }

    @Override
    public void generate(User user) {

        // --- 1. Business Logic: Token Generation & Persistence ---
        LocalTime expiration = LocalTime.now().plusMinutes(1    );
        VerificationToken emailVerificationToken = new VerificationToken();

        emailVerificationToken.setToken(RandomUtil.generate6Digits());
        emailVerificationToken.setExpiration(expiration);
        emailVerificationToken.setUser(user);

        user.setTokenDate(LocalDate.now());

        emailVerificationTokenRepository.save(emailVerificationToken);

        // --- 2. View Logic: Prepare Context & Render Email Content ---

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("verificationCode", emailVerificationToken.getToken());

        log.info("Verification Code: {}", emailVerificationToken.getToken());

        // Render the email content using the Thymeleaf template
        // 'templateEngine' and 'templateEngine.process()' are assumed to be defined/available
        String emailContent = templateEngine.process("email/verification-code.html", context);
        log.info("Rendered email content: {}", emailContent);

        try {
            String recipientEmail = user.getEmail();
            String subject = "Online Survey Email Verification";

            sendMail(recipientEmail, subject, emailContent);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong sending the email", e);
        }

        userRepository.save(user);
    }

    /**
     * Sends an email using the provided recipient, subject, and HTML content.
     *
     * @param to The recipient's email address.
     * @param subject The subject line of the email.
     * @param content The HTML content of the email body.
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // 'true' for multipart, 'UTF-8' for encoding

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(content, true); // 'true' indicates the content is HTML

            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Error sending email", e);
        }
    }

    @Override
    public boolean isExpired(VerificationToken token) {

        return !token.getExpiration().isBefore(LocalTime.now());
    }

}
package com.setec.online_survey.features.send_mail;

import com.setec.online_survey.domain.PasswordResetToken;
import com.setec.online_survey.domain.User;
import com.setec.online_survey.domain.VerificationToken;
import com.setec.online_survey.features.password_reset_verify.PasswordResetEmailVerifyRepository;
import com.setec.online_survey.features.register_email_verify.RegisterEmailVerifyRepository;
import com.setec.online_survey.features.user.UserRepository;
import com.setec.online_survey.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class SendMailServiceImpl implements SendMailService {

    private final RegisterEmailVerifyRepository registerEmailVerifyRepository;
    private final PasswordResetEmailVerifyRepository passwordResetEmailVerifyRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void generateRegisterOtp(User user) {

        LocalTime expiration = LocalTime.now().plusMinutes(1);
        String token = (RandomUtil.generate6Digits());

        VerificationToken emailVerificationToken = new VerificationToken();
        emailVerificationToken.setToken(passwordEncoder.encode(token));
        emailVerificationToken.setExpiration(expiration);
        emailVerificationToken.setUser(user);

        user.setTokenDate(LocalDate.now());

        sendMail(user.getEmail(),token,"Email Register verify");

        registerEmailVerifyRepository.save(emailVerificationToken);
        userRepository.save(user);
    }

    @Override
    public void generateResetPasswordOtp(User user) {

        var expiration = LocalDateTime.now().plusMinutes(1);
        String token = (RandomUtil.generate6Digits());

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(passwordEncoder.encode(token));
        passwordResetToken.setExpiration(expiration);
        passwordResetToken.setUser(user);

        user.setTokenDate(LocalDate.now());

        sendMail(user.getEmail(),token,"Email Password Request Verify");

        passwordResetEmailVerifyRepository.save(passwordResetToken);
        userRepository.save(user);
    }

    private void sendMail( String to,String token,  String subject) {

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("verificationCode", token);

        log.info("Verification Code: {}", token);

        // Render the email content using the Thymeleaf template
        // 'templateEngine' and 'templateEngine.process()' are assumed to be defined/available
        String emailContent = templateEngine.process("email/verification-code.html", context);
        log.info("Rendered email content: {}", emailContent);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // 'true' for multipart, 'UTF-8' for encoding

            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(emailContent, true); // 'true' indicates the content is HTML

            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Error sending email", e);
        }
    }
}



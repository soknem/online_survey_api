package com.setec.online_survey.features.test;

import com.setec.online_survey.features.mail.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailVerificationTokenService emailVerificationTokenService;

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String getString(){
        return "Home Page";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String getStrings(){
        return "hello ";
    }

    @PostMapping
    public String testPost(@RequestBody String body){
        return body;
    }

    @GetMapping("/send-mail")
    public void sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String content){
        emailVerificationTokenService.sendMail(to,subject,content);
    }
}

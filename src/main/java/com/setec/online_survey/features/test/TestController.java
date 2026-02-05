package com.setec.online_survey.features.test;

import com.setec.online_survey.features.send_mail.SendMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final SendMailService sendMailService;

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

    @GetMapping("/send")
    public String send(){
        //sendMailService.sendMail(to,subject,content);

        return "send";
    }

    @GetMapping("/send/1")
    public String send1(){
        //sendMailService.sendMail(to,subject,content);
        return "send1";
    }

    @GetMapping("/send/{message}")
    public String sendMessage(@PathVariable String message){
        //sendMailService.sendMail(to,subject,content);
        return "sendMessage";
    }
}

package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.member.EmailDto;
import com.umc.refit.domain.dto.member.EmailResDto;
import com.umc.refit.web.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final EmailService emailService;

    /*이메일 인증 API*/
    @PostMapping("/email")
    public EmailResDto email(@RequestBody EmailDto emailDto) throws MessagingException {

        String auth = emailService.sendEmail(emailDto.getEmail());

        return new EmailResDto(auth);
    }
}

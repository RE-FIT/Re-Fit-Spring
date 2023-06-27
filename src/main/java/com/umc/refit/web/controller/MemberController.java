package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.member.EmailDto;
import com.umc.refit.web.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final EmailService emailService;

    /*이메일 인증 API*/
    @PostMapping("/email")
    public String email(@RequestBody EmailDto emailDto) {

        emailService.sendEmail("cswcsm02@gmail.com", "Hello", "This is a test email.");

        return emailDto.getEmail();
    }
}

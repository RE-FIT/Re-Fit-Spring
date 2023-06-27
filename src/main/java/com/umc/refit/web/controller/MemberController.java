package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.member.EmailDto;
import com.umc.refit.domain.dto.member.EmailResDto;
import com.umc.refit.exception.member.EmailException;
import com.umc.refit.exception.validator.EmailValidator;
import com.umc.refit.web.service.EmailService;
import com.umc.refit.web.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

import static com.umc.refit.exception.ExceptionType.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final EmailService emailService;
    private final MemberService memberService;

    /*이메일 인증 API*/
    @PostMapping("/email")
    public EmailResDto email(@RequestBody EmailDto emailDto) throws MessagingException {

        String email = emailDto.getEmail();

        //예외 1. 이메일이 비어있을 경우
        if (email.strip().equals("")) {
            throw new EmailException(EMAIL_EMPTY, EMAIL_EMPTY.getCode(), EMAIL_EMPTY.getErrorMessage());
        }

        //예외 2. 이미 존재하는 회원일 경우
        if (memberService.findMember(email).isPresent()) {
            throw new EmailException(EMAIL_ALREADY_EXIST, EMAIL_ALREADY_EXIST.getCode(), EMAIL_ALREADY_EXIST.getErrorMessage());
        }

        //예외 3. 이메일 형식에 맞지 않을 경우
        if (!EmailValidator.isValid(email)) {
            throw new EmailException(EMAIL_INVALID, EMAIL_INVALID.getCode(), EMAIL_INVALID.getErrorMessage());
        }

        String auth = emailService.sendEmail(emailDto.getEmail());

        return new EmailResDto(auth);
    }
}

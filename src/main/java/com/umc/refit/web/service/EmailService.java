package com.umc.refit.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private String authCode;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    //8자리 숫자로 된 랜덤 코드 생성 메서드
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int digit = random.nextInt(10);
            code.append(digit);
        }
        authCode = code.toString();
        return authCode;
    }

    //메일 양식 작성 메서드
    public MimeMessage joinEmailForm(String email) throws MessagingException {

        String code = generateCode(); //인증 코드 생성
        String title = "리핏 회원가입 인증번호 이메일 입니다.";

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email); //보낼 이메일 설정
        message.setSubject(title);
        message.setFrom(from);

        //타임리프를 통해 본문 내용 구성
        Context context = new Context();
        context.setVariable("code", code);
        String process = templateEngine.process("verificationCode", context);
        message.setText(process, "utf-8", "html");

        return message;
    }

    //이메일 전송 메서드
    public String sendEmail(String to) throws MessagingException {

        MimeMessage emailForm = joinEmailForm(to);
        mailSender.send(emailForm);
        return authCode;
    }
}


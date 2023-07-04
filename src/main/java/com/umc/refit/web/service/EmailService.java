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
import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /*인증번호 필드*/
    private String authCode;

    /*비밀번호 재설정 필드*/
    private static final String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!_-";
    private static final String ALL_CHARACTERS = UPPERCASE_CHARACTERS + LOWERCASE_CHARACTERS + DIGITS + SPECIAL_CHARACTERS;
    private static final Random RANDOM = new SecureRandom();
    private String resetPassword;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 회원 인증번호 로직
     * */
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

    //인증코드 메일 양식 작성 메서드
    public MimeMessage joinEmailForm(String email) throws MessagingException {

        String code = generateCode(); //인증 코드 생성
        String title = "[RE-FIT] 회원가입 인증번호 안내 이메일 입니다.";

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

    //인증번호 이메일 전송 메서드
    public String sendEmail(String to) throws MessagingException {

        MimeMessage emailForm = joinEmailForm(to);
        mailSender.send(emailForm);
        return authCode;
    }

    /**
     * 회원 비밀번호 재설정 로직
     * */
    //8자리 숫자로 된 랜덤 코드 생성 메서드
    public String generateResetPassword() {
        StringBuilder result = new StringBuilder(8);

        // Generate the initial random string
        for (int i = 0; i < 8; i++) {
            result.append(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
        }

        // Ensure the string contains at least one character from each required set
        result.setCharAt(0, UPPERCASE_CHARACTERS.charAt(RANDOM.nextInt(UPPERCASE_CHARACTERS.length())));
        result.setCharAt(1, LOWERCASE_CHARACTERS.charAt(RANDOM.nextInt(LOWERCASE_CHARACTERS.length())));
        result.setCharAt(2, DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        result.setCharAt(3, SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        resetPassword = result.toString();

        return resetPassword;
    }

    //비밀번호 재설정 메일 양식 작성 메서드
    public MimeMessage resetEmailForm(String email, String name) throws MessagingException {

        String code = generateResetPassword(); //인증 코드 생성
        String title = "[RE-FIT] 임시 비밀번호 안내 이메일 입니다.";

        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email); //보낼 이메일 설정
        message.setSubject(title);
        message.setFrom(from);

        //타임리프를 통해 본문 내용 구성
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("name", name);
        String process = templateEngine.process("passwordReset", context);
        message.setText(process, "utf-8", "html");

        return message;
    }

    //비밀번호 재설정 이메일 전송 메서드
    public String resetEmail(String to, String name) throws MessagingException {
        MimeMessage emailForm = resetEmailForm(to, name);
        mailSender.send(emailForm);
        return resetPassword;
    }
}


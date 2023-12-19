package com.umc.refit.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.refit.domain.dto.member.*;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.MemberException;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.exception.validator.MemberValidator;
import com.umc.refit.web.service.EmailService;
import com.umc.refit.web.service.MemberService;

import com.umc.refit.web.service.ValidateService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.umc.refit.exception.ExceptionType.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final EmailService emailService;
    private final MemberService memberService;
    private final ValidateService validateService;

    @Value("${member.image}")
    private String imageUrl;

    @PostMapping("/email")
    public ResEmailDto email(@RequestBody EmailDto emailDto) throws MessagingException {
        String email = emailDto.getEmail();
        validateService.emailCheck(email);
        String auth = emailService.sendEmail(emailDto.getEmail());
        return new ResEmailDto(auth);
    }

    @PostMapping("/join/name")
    public void checkName(@RequestBody NameDto nameDto) {
        String name = nameDto.getName();
        validateService.nameCheck(name);
    }

    @PostMapping("/join")
    public void join(@RequestBody JoinDto joinDto) {
        validateService.validateJoin(joinDto);
        memberService.save(new Member(joinDto, imageUrl));
    }

    @PostMapping("/reset/password")
    public void reset(@RequestBody PasswordResetDto dto) throws MessagingException {
        Optional<Member> member = memberService.findMemberForPasswordRest(dto.getName(), dto.getEmail(), dto.getLoginId());

        if (member.isEmpty()) {
            throw new MemberException(PASSWORD_RESET_FAIL, PASSWORD_RESET_FAIL.getCode(), PASSWORD_RESET_FAIL.getErrorMessage());
        }

        if (member.get().getSocialType() != null) {
            throw new MemberException(PASSWORD_RESET_FAIL, PASSWORD_RESET_FAIL.getCode(), PASSWORD_RESET_FAIL.getErrorMessage());
        }

        String password = emailService.resetEmail(dto.getEmail(), dto.getName());

        Member getMember = member.get();
        getMember.setPassword(password);
        memberService.save(getMember);
    }

    @PostMapping("/find/id")
    public ResIdFindDto findId(@RequestBody IdFindDto idFindDto) {

        String email = idFindDto.getEmail();
        String name = idFindDto.getName();

        Optional<Member> member = memberService.findMemberForFindId(name, email);

        if (member.isEmpty()) {
            throw new MemberException(MEMBER_IS_NOT_EXIST, MEMBER_IS_NOT_EXIST.getCode(), MEMBER_IS_NOT_EXIST.getErrorMessage());
        }

        String lastThreeReplaced = "***";
        String exceptLastThree = member.get().getLoginId().substring(0, member.get().getLoginId().length() - 3);
        return new ResIdFindDto(exceptLastThree + lastThreeReplaced);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kakao")
    public ResponseEntity<Void> kakao_login() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletRequest request) {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        String email = authentication.getName();
        memberService.deleteRefreshToken(email);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/token/check")
    public ResponseEntity<Void> token_check() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> token_reissue(Authentication authentication, HttpServletRequest request
            , HttpServletResponse response) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        TokenDto tokenResponse = memberService.refreshAuthenticationToken(authentication);

        response.addHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());
        response.setContentType("application/json");

        RefreshTokenDto responseDto = new RefreshTokenDto(tokenResponse.getRefreshToken());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(responseDto);
        response.getWriter().write(jsonString);
        return ResponseEntity.ok().build();
    }
}

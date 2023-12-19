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

    @Value("${member.image}")
    private String imageUrl;

    /*이메일 인증 API*/
    @PostMapping("/email")
    public ResEmailDto email(@RequestBody EmailDto emailDto) throws MessagingException {

        String email = emailDto.getEmail();
        emailCheck(email);

        String auth = emailService.sendEmail(emailDto.getEmail());

        return new ResEmailDto(auth);
    }

    @PostMapping("/join/name")
    public void checkName(@RequestBody NameDto nameDto) {
        String name = nameDto.getName();
        nameCheck(name);
    }

    /*회원 가입 API*/
    @PostMapping("/join")
    public void join(@RequestBody JoinDto joinDto) {

        String loginId = joinDto.getLoginId();
        String password = joinDto.getPassword();
        String email = joinDto.getEmail();
        String name = joinDto.getName();
        String birth = joinDto.getBirth();
        Integer gender = joinDto.getGender();

        /*예외 체크*/
        loginIdCheck(loginId);
        passwordCheck(password);
        emailCheck(email);
        nameCheck(name);
        birthCheck(birth);
        genderCheck(gender);

        /*예외 처리가 끝나면 회원 저장*/
        memberService.save(new Member(joinDto, imageUrl));
    }

    /*이메일 인증 API*/
    @PostMapping("/reset/password")
    public void reset(@RequestBody PasswordResetDto passwordResetDto) throws MessagingException {

        String email = passwordResetDto.getEmail();
        String name = passwordResetDto.getName();
        String loginId = passwordResetDto.getLoginId();

        Optional<Member> member = memberService.findMemberForPasswordRest(name, email, loginId);

        if (member.isEmpty()) {
            throw new MemberException(PASSWORD_RESET_FAIL, PASSWORD_RESET_FAIL.getCode(), PASSWORD_RESET_FAIL.getErrorMessage());
        }

        if (member.get().getSocialType() != null) {
            throw new MemberException(PASSWORD_RESET_FAIL, PASSWORD_RESET_FAIL.getCode(), PASSWORD_RESET_FAIL.getErrorMessage());
        }

        String password = emailService.resetEmail(email, name);

        Member getMember = member.get();
        getMember.setPassword(password);
        memberService.save(getMember);
    }

    /*아이디 찾기 API*/
    @PostMapping("/find/id")
    public ResIdFindDto findId(@RequestBody IdFindDto idFindDto) {

        String email = idFindDto.getEmail();
        String name = idFindDto.getName();

        /*예외 처리가 끝나면 회원 조회*/
        Optional<Member> member = memberService.findMemberForFindId(name, email);

        /*회원이 조회되지 않으면 예외*/
        if (member.isEmpty()) {
            throw new MemberException(MEMBER_IS_NOT_EXIST, MEMBER_IS_NOT_EXIST.getCode(), MEMBER_IS_NOT_EXIST.getErrorMessage());
        }

        /*뒤 세글자 ***로 변환*/
        String lastThreeReplaced = "***";
        String exceptLastThree = member.get().getLoginId().substring(0, member.get().getLoginId().length() - 3);

        return new ResIdFindDto(exceptLastThree + lastThreeReplaced);
    }

    /*일반 로그인 API*/
    @PostMapping("/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.ok().build();
    }

    /*카카오 로그인 API*/
    @PostMapping("/kakao")
    public ResponseEntity<Void> kakao_login() {
        return ResponseEntity.ok().build();
    }

    /*로그아웃 API*/
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

    /*엑세스 토큰 체크 API*/
    @GetMapping("/token/check")
    public ResponseEntity<Void> token_check() {
        return ResponseEntity.ok().build();
    }

    /*토큰 재발급 API*/
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

    /*로그인 아이디 체크 메서드*/
    private void loginIdCheck(String loginId) {
        //예외 코드 10011: 아이디가 비어있을 경우
        if (loginId.strip().equals("")) {
            throw new MemberException(ID_EMPTY, ID_EMPTY.getCode(), ID_EMPTY.getErrorMessage());
        }

        //예외 코드 10012: 아이디가 형식에 맞지 않은 경우
        if (!MemberValidator.isLoginValid(loginId)) {
            throw new MemberException(ID_INVALID, ID_INVALID.getCode(), ID_INVALID.getErrorMessage());
        }

        //예외 코드 10013: 아이디가 이미 존재하는 경우
        if (memberService.findMemberByLoginId(loginId).isPresent()) {
            throw new MemberException(ID_ALREADY_EXIST, ID_ALREADY_EXIST.getCode(), ID_ALREADY_EXIST.getErrorMessage());
        }
    }

    /*비밀번호 체크 메서드*/
    private void passwordCheck(String password) {
        //예외 코드 10014: 비밀번호는 필수 정보입니다.
        if (password.strip().equals("")) {
            throw new MemberException(PASSWORD_EMPTY, PASSWORD_EMPTY.getCode(), PASSWORD_EMPTY.getErrorMessage());
        }

        //예외 코드 10015: "8-16자의 영문 대소문자, 숫자, 특수문자 ((!), (_) , (-))를 포합해야합니다.
        if (!MemberValidator.isPasswordValid(password)) {
            throw new MemberException(PASSWORD_INVALID, PASSWORD_INVALID.getCode(), PASSWORD_INVALID.getErrorMessage());
        }
    }

    /*이메일 체크 메서드*/
    private void emailCheck(String email) {
        //예외 코드 10016: 이메일이 비어있을 경우
        if (email.strip().equals("")) {
            throw new MemberException(EMAIL_EMPTY, EMAIL_EMPTY.getCode(), EMAIL_EMPTY.getErrorMessage());
        }

        //예외 코드 10017: 이미 존재하는 회원일 경우
        if (memberService.findMemberByEmail(email).isPresent()) {
            throw new MemberException(EMAIL_ALREADY_EXIST, EMAIL_ALREADY_EXIST.getCode(), EMAIL_ALREADY_EXIST.getErrorMessage());
        }

        //예외 코드 10018: 이메일 형식에 맞지 않을 경우
        if (!MemberValidator.isEmailValid(email)) {
            throw new MemberException(EMAIL_INVALID, EMAIL_INVALID.getCode(), EMAIL_INVALID.getErrorMessage());
        }
    }

    /*닉네임 체크 메서드*/
    private void nameCheck(String name) {
        //예외 코드 10019: 이름이 비어있을 경우
        if (name.strip().equals("")) {
            throw new MemberException(NAME_EMPTY, NAME_EMPTY.getCode(), NAME_EMPTY.getErrorMessage());
        }

        //예외 코드 10020: 이미 존재하는 이름일 경우
        if (memberService.findMemberByName(name).isPresent()) {
            throw new MemberException(NAME_ALREADY_EXIST, NAME_ALREADY_EXIST.getCode(), NAME_ALREADY_EXIST.getErrorMessage());
        }
    }

    /*생일 체크 메서드*/
    private void birthCheck(String birth) {
        //예외 코드 10021: 생일이 비어있을 경우
        if (birth.strip().equals("")) {
            throw new MemberException(BIRTH_EMPTY, BIRTH_EMPTY.getCode(), BIRTH_EMPTY.getErrorMessage());
        }

        //예외 코드 10022: 생일이 형식에 맞지 않는 경우
        if (!MemberValidator.isBirthValid(birth)) {
            throw new MemberException(BIRTH_ALREADY_EXIST, BIRTH_ALREADY_EXIST.getCode(), BIRTH_ALREADY_EXIST.getErrorMessage());
        }
    }

    /*성별 체크 메서드*/
    private void genderCheck(Integer gender) {
        //예외 코드 10021: 생일이 비어있을 경우
        if (gender == null) {
            throw new MemberException(GENDER_EMPTY, GENDER_EMPTY.getCode(), GENDER_EMPTY.getErrorMessage());
        }
    }
}

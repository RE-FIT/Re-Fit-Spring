package com.umc.refit.web.service;

import com.umc.refit.domain.dto.member.JoinDto;
import com.umc.refit.exception.member.MemberException;
import com.umc.refit.exception.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.umc.refit.exception.ExceptionType.*;
import static com.umc.refit.exception.ExceptionType.GENDER_EMPTY;

@Service
@RequiredArgsConstructor
public class ValidateService {

    private final MemberService memberService;

    public void validateJoin(JoinDto joinDto) {
        loginIdCheck(joinDto.getLoginId());
        passwordCheck(joinDto.getPassword());
        emailCheck(joinDto.getEmail());
        nameCheck(joinDto.getName());
        birthCheck(joinDto.getBirth());
        genderCheck(joinDto.getGender());
    }

    public void loginIdCheck(String loginId) {
        if (loginId.strip().equals("")) {
            throw new MemberException(ID_EMPTY, ID_EMPTY.getCode(), ID_EMPTY.getErrorMessage());
        }

        if (!MemberValidator.isLoginValid(loginId)) {
            throw new MemberException(ID_INVALID, ID_INVALID.getCode(), ID_INVALID.getErrorMessage());
        }

        if (memberService.findMemberByLoginId(loginId).isPresent()) {
            throw new MemberException(ID_ALREADY_EXIST, ID_ALREADY_EXIST.getCode(), ID_ALREADY_EXIST.getErrorMessage());
        }
    }

    public void passwordCheck(String password) {
        if (password.strip().equals("")) {
            throw new MemberException(PASSWORD_EMPTY, PASSWORD_EMPTY.getCode(), PASSWORD_EMPTY.getErrorMessage());
        }

        if (!MemberValidator.isPasswordValid(password)) {
            throw new MemberException(PASSWORD_INVALID, PASSWORD_INVALID.getCode(), PASSWORD_INVALID.getErrorMessage());
        }
    }

    public void emailCheck(String email) {
        if (email.strip().equals("")) {
            throw new MemberException(EMAIL_EMPTY, EMAIL_EMPTY.getCode(), EMAIL_EMPTY.getErrorMessage());
        }

        if (memberService.findMemberByEmail(email).isPresent()) {
            throw new MemberException(EMAIL_ALREADY_EXIST, EMAIL_ALREADY_EXIST.getCode(), EMAIL_ALREADY_EXIST.getErrorMessage());
        }

        if (!MemberValidator.isEmailValid(email)) {
            throw new MemberException(EMAIL_INVALID, EMAIL_INVALID.getCode(), EMAIL_INVALID.getErrorMessage());
        }
    }

    public void nameCheck(String name) {
        if (name.strip().equals("")) {
            throw new MemberException(NAME_EMPTY, NAME_EMPTY.getCode(), NAME_EMPTY.getErrorMessage());
        }

        if (memberService.findMemberByName(name).isPresent()) {
            throw new MemberException(NAME_ALREADY_EXIST, NAME_ALREADY_EXIST.getCode(), NAME_ALREADY_EXIST.getErrorMessage());
        }
    }

    public void birthCheck(String birth) {
        if (birth.strip().equals("")) {
            throw new MemberException(BIRTH_EMPTY, BIRTH_EMPTY.getCode(), BIRTH_EMPTY.getErrorMessage());
        }

        if (!MemberValidator.isBirthValid(birth)) {
            throw new MemberException(BIRTH_ALREADY_EXIST, BIRTH_ALREADY_EXIST.getCode(), BIRTH_ALREADY_EXIST.getErrorMessage());
        }
    }

    public void genderCheck(Integer gender) {
        if (gender == null) {
            throw new MemberException(GENDER_EMPTY, GENDER_EMPTY.getCode(), GENDER_EMPTY.getErrorMessage());
        }
    }
}

package com.umc.refit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    /*회원 관련 예외*/

    //회원 가입 예외
    ID_EMPTY(BAD_REQUEST, 10011, "아이디는 필수 정보입니다."),
    ID_INVALID(BAD_REQUEST, 10012, "8~16자의 영문, 숫자를 포함해야 합니다."),
    ID_ALREADY_EXIST(BAD_REQUEST, 10013, "이미 존재하는 아이디입니다."),
    PASSWORD_EMPTY(BAD_REQUEST, 10014, "비밀번호는 필수 정보입니다."),
    PASSWORD_INVALID(BAD_REQUEST, 10015, "8-16자의 영문 대소문자, 숫자, 특수문자 ((!), (_) , (-))를 포합해야합니다."),
    NAME_EMPTY(BAD_REQUEST, 10019, "이름은 필수 정보입니다."),
    NAME_ALREADY_EXIST(BAD_REQUEST, 10020, "이미 존재하는 이름입니다."),

    //회원 이메일 예외
    EMAIL_EMPTY(BAD_REQUEST, 10016, "이메일은 필수 정보입니다."),
    EMAIL_ALREADY_EXIST(BAD_REQUEST, 10017, "이미 존재하는 이메일입니다."),
    EMAIL_INVALID(BAD_REQUEST, 10018, "이메일 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String errorMessage;
}
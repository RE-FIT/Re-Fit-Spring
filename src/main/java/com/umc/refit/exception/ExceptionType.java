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

    //회원 이메일 예외
    EMAIL_EMPTY(BAD_REQUEST, 10016, "이메일은 필수 정보입니다."),
    EMAIL_ALREADY_EXIST(BAD_REQUEST, 10017, "이미 존재하는 이메일입니다."),
    EMAIL_INVALID(BAD_REQUEST, 10018, "이메일 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String errorMessage;
}
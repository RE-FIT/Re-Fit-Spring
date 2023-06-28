package com.umc.refit.exception.member;

import com.umc.refit.exception.ExceptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class JoinException extends RuntimeException {

    private ExceptionType exceptionType;
    private int code;
    private String errorMessage;
}

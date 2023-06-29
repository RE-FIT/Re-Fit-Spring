package com.umc.refit.exception.member;

import com.umc.refit.exception.ExceptionType;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class LoginException extends AuthenticationException {

    private ExceptionType errorType;
    private int code;
    private String errorMessage;

    public LoginException(ExceptionType errorType, int code, String errorMessage) {
        super("");
        this.errorType = errorType;
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

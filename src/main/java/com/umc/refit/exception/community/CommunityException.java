package com.umc.refit.exception.community;

import com.umc.refit.exception.ExceptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CommunityException extends RuntimeException {

    private ExceptionType exceptionType;
    private int code;
    private String errorMessage;

}

package com.umc.refit.exception.handler;

import com.umc.refit.exception.ErrorResult;
import com.umc.refit.exception.clothe.ClotheException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class ClotheExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClotheException.class)
    public ErrorResult ClotheExceptionHandle(ClotheException e, HttpServletRequest request) {
        log.error("[CustomException] url: {} | errorType: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getExceptionType(), e.getMessage(), e.getCause());
        return new ErrorResult(String.valueOf(e.getCode()), e.getErrorMessage());
    }
}

package com.umc.refit.exception.handler;

import com.umc.refit.exception.ErrorResult;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.exception.community.FileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CommunityExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CommunityException.class)
    public ErrorResult CommunityExceptionHandle(CommunityException e, HttpServletRequest request) {
        log.error("[CustomException] url: {} | errorType: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getExceptionType(), e.getMessage(), e.getCause());
        return new ErrorResult(String.valueOf(e.getCode()), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FileException.class)
    public ErrorResult FileExceptionHandle(FileException e, HttpServletRequest request) {
        log.error("[CustomException] url: {} | errorType: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getExceptionType(), e.getMessage(), e.getCause());
        return new ErrorResult(String.valueOf(e.getCode()), e.getErrorMessage());
    }



}

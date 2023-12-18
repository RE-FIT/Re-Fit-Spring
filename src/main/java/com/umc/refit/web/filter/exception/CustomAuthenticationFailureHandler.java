package com.umc.refit.web.filter.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.refit.exception.member.LoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

import static com.umc.refit.exception.ExceptionType.LOGIN_FAILED;
import static com.umc.refit.exception.ExceptionType.LOGIN_FAILED_ALL;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json;charset=UTF-8");

        String errorMessage;
        int code;

        if ((exception instanceof UsernameNotFoundException) || (exception instanceof BadCredentialsException)){
            errorMessage = LOGIN_FAILED.getErrorMessage();
            code = LOGIN_FAILED.getCode();
        } else if (exception instanceof LoginException) {
            errorMessage = ((LoginException) exception).getErrorMessage();
            code = ((LoginException) exception).getCode();
        } else {
            errorMessage = LOGIN_FAILED_ALL.getErrorMessage();
            code = LOGIN_FAILED_ALL.getCode();
        }

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", errorMessage);
        errorResponse.put("code", code);

        String jsonResponse = mapper.writeValueAsString(errorResponse);
        response.getWriter().print(jsonResponse);
    }
}
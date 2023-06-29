package com.umc.refit.web.filter.exception;

import com.nimbusds.jose.shaded.json.JSONObject;
import com.umc.refit.exception.member.LoginException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

            if (exception instanceof UsernameNotFoundException) {
                System.out.println("하이하이");
            } else  {
                System.out.println("이하이하");
            }


            errorMessage = LOGIN_FAILED.getErrorMessage();
            code = LOGIN_FAILED.getCode();
        } else if (exception instanceof LoginException) {
            errorMessage = ((LoginException) exception).getErrorMessage();
            code = ((LoginException) exception).getCode();
        } else {
            errorMessage = LOGIN_FAILED_ALL.getErrorMessage();
            code = LOGIN_FAILED_ALL.getCode();
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("errorMessage", errorMessage);
        responseJson.put("code", code);
        response.getWriter().print(responseJson);
    }
}
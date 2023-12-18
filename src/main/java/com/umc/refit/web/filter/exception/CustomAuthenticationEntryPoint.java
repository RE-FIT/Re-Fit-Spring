package com.umc.refit.web.filter.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.refit.exception.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ExceptionType exception = (ExceptionType) request.getAttribute("exception");
        setResponse(response, exception);
    }

    private void setResponse(HttpServletResponse response, ExceptionType errorType) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", errorType.getErrorMessage());
        errorResponse.put("code", errorType.getCode());

        String jsonResponse = mapper.writeValueAsString(errorResponse);
        response.getWriter().print(jsonResponse);
    }
}

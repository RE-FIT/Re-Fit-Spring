package com.umc.refit.web.filter.entrypoint;

import com.nimbusds.jose.shaded.json.JSONObject;
import com.umc.refit.exception.ExceptionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

        JSONObject responseJson = new JSONObject();
        responseJson.put("errorMessage", errorType.getErrorMessage());
        responseJson.put("code", errorType.getCode());
        response.getWriter().print(responseJson);
    }
}

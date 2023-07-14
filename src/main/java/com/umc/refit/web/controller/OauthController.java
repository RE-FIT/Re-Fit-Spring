package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.chat.OAuth2;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequiredArgsConstructor
public class OauthController {

    @GetMapping("/oauth2")
    public OAuth2 auth(Authentication authentication, HttpServletRequest request) {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        return new OAuth2(authentication.getName());
    }
}

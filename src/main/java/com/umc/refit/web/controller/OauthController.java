package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.chat.OAuth2;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class OauthController {

    @GetMapping("/oauth2")
    public OAuth2 auth(Authentication authentication) {
        return new OAuth2(authentication.getName());
    }

}

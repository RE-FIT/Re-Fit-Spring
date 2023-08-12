package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.chat.FCM;
import com.umc.refit.domain.dto.chat.OAuth2;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class OauthController {

    private final MemberService memberService;

    @GetMapping("/oauth2")
    public OAuth2 auth(Authentication authentication, HttpServletRequest request) {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        Optional<Member> memberByLoginId = memberService.findMemberByLoginId(authentication.getName());
        Member member = memberByLoginId.get();

        return new OAuth2(member.getName());
    }

    @GetMapping("/oauth2/fcm")
    public FCM getFcm(HttpServletRequest request) {

        String otherId = request.getParameter("otherId");

        Optional<Member> other = memberService.findMemberByName(otherId);

        return new FCM(other.get().getFcm());
    }
}

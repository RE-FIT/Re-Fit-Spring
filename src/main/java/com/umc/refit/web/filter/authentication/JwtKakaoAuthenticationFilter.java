package com.umc.refit.web.filter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.umc.refit.domain.dto.member.RefreshTokenDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.member.LoginException;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.service.RefreshTokenService;
import com.umc.refit.web.signature.JWTSigner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import static com.umc.refit.Util.*;
import static com.umc.refit.Util.KAKAO_API;
import static com.umc.refit.exception.ExceptionType.*;

@RequiredArgsConstructor
public class JwtKakaoAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final JWTSigner securitySigner;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;
    private final RestTemplate restTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String accessToken = request.getHeader("Authorization");
        String fcm = request.getParameter("fcm");

        String userEmail = getEmailFromKakao(accessToken);
        findOrCreateMember(userEmail, fcm);

        return authenticateUser(userEmail);
    }

    private Member findOrCreateMember(String userEmail, String fcm) {
        return memberService.findMemberByEmail(userEmail)
                .map(member -> updateMemberFcm(member, fcm))
                .orElseGet(() -> createNewMember(userEmail, fcm));
    }

    private Member updateMemberFcm(Member member, String fcm) {
        if (!("KAKAO".equals(member.getSocialType()))) {
            throw new LoginException(BASIC_MEMBER_EXIST,
                    BASIC_MEMBER_EXIST.getCode(), BASIC_MEMBER_EXIST.getErrorMessage());
        }
        member.setFcm(fcm);
        memberService.updateFcm(member);
        return member;
    }

    private Member createNewMember(String userEmail, String fcm) {
        String name;
        do {
            String randomString = generateRandomString(4);
            name = "환경지킴이" + randomString;
        } while (memberService.findMemberByName(name).isPresent());

        Member member = new Member(userEmail, name, fcm);
        memberService.kakaoSave(member);
        return member;
    }

    private Authentication authenticateUser(String userEmail) {
        AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userEmail, userEmail);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {
        User user = (User) authResult.getPrincipal();

        String accessToken = securitySigner.getJwtToken(user, ONE_HOUR);
        String refreshToken = securitySigner.getJwtToken(user, ONE_WEEK);

        response.addHeader("Authorization", "Bearer " + accessToken);

        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);

        RefreshTokenDto resEmailDto = new RefreshTokenDto(refreshToken);
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(resEmailDto);
        response.getWriter().write(jsonString);
    }

    private String getEmailFromKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_API, HttpMethod.POST, entity, String.class);

            JsonElement element = JsonParser.parseString(response.getBody());
            return element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();
        } catch (RestClientException e) {
            throw new LoginException(LOGIN_FAILED_UNKNOWN,
                    LOGIN_FAILED_UNKNOWN.getCode(), LOGIN_FAILED_UNKNOWN.getErrorMessage());
        }
    }
}

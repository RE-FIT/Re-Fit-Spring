package com.umc.refit.web.filter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.refit.domain.dto.member.LoginDto;
import com.umc.refit.domain.dto.member.RefreshTokenDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.member.LoginException;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.service.RefreshTokenService;
import com.umc.refit.web.signature.JWTSigner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Optional;

import static com.umc.refit.Util.ONE_HOUR;
import static com.umc.refit.Util.ONE_WEEK;
import static com.umc.refit.exception.ExceptionType.*;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final JWTSigner securitySigner;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginDto loginDto = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);

            String loginId = loginDto.getLoginId();
            String password = loginDto.getPassword();
            String fcm = loginDto.getFcm();

            Optional<Member> findMember = memberService.findMemberByLoginId(loginId);

            findMember.ifPresent(member -> {
                validateSocialMember(member);
                member.setFcm(fcm);
                memberService.updateFcm(member);
            });

            AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(findMember.get().getEmail(), password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (IOException e) {
            throw new LoginException(LOGIN_FAILED_UNKNOWN,
                    LOGIN_FAILED_UNKNOWN.getCode(), LOGIN_FAILED_UNKNOWN.getErrorMessage());
        }
    }

    private void validateSocialMember(Member member) {
        if ("KAKAO".equals(member.getSocialType())) {
            throw new LoginException(KAKAO_MEMBER_EXIST,
                    KAKAO_MEMBER_EXIST.getCode(), KAKAO_MEMBER_EXIST.getErrorMessage());
        }

        if ("GOOGLE".equals(member.getSocialType())) {
            throw new LoginException(GOOGLE_MEMBER_EXIST,
                    GOOGLE_MEMBER_EXIST.getCode(), GOOGLE_MEMBER_EXIST.getErrorMessage());
        }

        if ("NAVER".equals(member.getSocialType())) {
            throw new LoginException(NAVER_MEMBER_EXIST,
                    NAVER_MEMBER_EXIST.getCode(), NAVER_MEMBER_EXIST.getErrorMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        User user = (User) authResult.getPrincipal();

        String accessToken = securitySigner.getJwtToken(user, ONE_HOUR);
        String refreshToken = securitySigner.getJwtToken(user, ONE_WEEK);

        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);

        response.addHeader("Authorization", "Bearer " + accessToken);

        RefreshTokenDto resEmailDto = new RefreshTokenDto(refreshToken);
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(resEmailDto);
        response.getWriter().write(jsonString);
    }
}

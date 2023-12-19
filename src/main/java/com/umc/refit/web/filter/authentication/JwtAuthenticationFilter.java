package com.umc.refit.web.filter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.refit.domain.dto.member.ResLoginDto;
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

import static com.umc.refit.exception.ExceptionType.KAKAO_MEMBER_EXIST;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final JWTSigner securitySigner;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String loginId = request.getParameter("loginId");
        String password = request.getParameter("password");
        String fcm = request.getParameter("fcm");

        Optional<Member> findMember = memberService.findMemberByLoginId(loginId);

        if (findMember.isPresent()) {
            if (!(findMember.get().getSocialType() == null)) {
                throw new LoginException(KAKAO_MEMBER_EXIST,
                        KAKAO_MEMBER_EXIST.getCode(), KAKAO_MEMBER_EXIST.getErrorMessage());
            }

            Member member = findMember.get();
            member.setFcm(fcm);
            memberService.updateFcm(member);
        }

        AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        User user = (User) authResult.getPrincipal();
        //엑세스 토큰 및 리프레쉬 토큰 발행
        String accessToken = securitySigner.getJwtToken(user, 216000000);
        String refreshToken = securitySigner.getJwtToken(user, 216000000);

        //리프레쉬 토큰 저장
        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);

        //엑세스 토큰 헤더를 통해 전달
        response.addHeader("Authorization", "Bearer " + accessToken); //발행받은 토큰을 response 헤더에 담아 응답

        //리프레쉬 토큰 바디에 담아 전달
        ResLoginDto resEmailDto = new ResLoginDto(refreshToken);
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(resEmailDto);
        response.getWriter().write(jsonString);
    }
}

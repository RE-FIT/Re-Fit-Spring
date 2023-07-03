package com.umc.refit.web.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.umc.refit.web.filter.exception.CustomAuthenticationFailureHandler;
import com.umc.refit.web.filter.authentication.CustomUserDetailsService;
import com.umc.refit.web.filter.authentication.JwtAuthenticationFilter;
import com.umc.refit.web.filter.authentication.JwtKakaoAuthenticationFilter;
import com.umc.refit.web.filter.authorization.JwtAuthorizationRsaFilter;
import com.umc.refit.web.filter.exception.CustomAuthenticationEntryPoint;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.service.RefreshTokenService;
import com.umc.refit.web.signature.RSASecuritySigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
public class OAuth2ResourceServer {

    private final RSASecuritySigner rsaSecuritySigner;
    private final RSAKey rsaKey;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler authFailureHandler;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        //세션을 사용하지 않음
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //인증을 거치지 않을 URL 처리 및 인증, 인가 예외 EntryPoint 등록
        http.authorizeRequests((requests) ->
                requests.antMatchers("/auth/logout" //로그아웃
                                , "/auth/join" //회원 가입
                                , "/auth/email" //이메일 찾기
                                , "/auth/reset/password" //패스워드 찾기
//                                , "/**"
                        ).permitAll()
                .anyRequest().authenticated())
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint());

        //사용자 정보 로드해서 객체 생성
        http.userDetailsService(userDetailsService);

        //일반 로그인 URL 설정
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(http, rsaSecuritySigner, rsaKey, memberService, refreshTokenService);
        jwtAuthenticationFilter.setAuthenticationFailureHandler(authFailureHandler);
        jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");

        //카카오 로그인 URL 설정
        JwtKakaoAuthenticationFilter jwtKakaoAuthenticationFilter =
                new JwtKakaoAuthenticationFilter(http, rsaSecuritySigner, rsaKey, memberService, refreshTokenService);
        jwtKakaoAuthenticationFilter.setAuthenticationFailureHandler(authFailureHandler);
        jwtKakaoAuthenticationFilter.setFilterProcessesUrl("/auth/kakao");

        //인가 필터 등록 필터
        http.addFilter(jwtAuthenticationFilter).addFilter(jwtKakaoAuthenticationFilter)
                .addFilterBefore(new JwtAuthorizationRsaFilter(rsaKey), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

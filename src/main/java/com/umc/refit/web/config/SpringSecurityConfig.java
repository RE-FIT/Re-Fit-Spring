package com.umc.refit.web.config;

import com.umc.refit.web.filter.authentication.CustomUserDetailsService;
import com.umc.refit.web.filter.authentication.JwtAuthenticationFilter;
import com.umc.refit.web.filter.authentication.JwtKakaoAuthenticationFilter;
import com.umc.refit.web.filter.authorization.JwtAuthorizationRsaFilter;
import com.umc.refit.web.filter.exception.CustomAuthenticationEntryPoint;
import com.umc.refit.web.filter.exception.CustomAuthenticationFailureHandler;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.service.RefreshTokenService;
import com.umc.refit.web.signature.JWTSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.Key;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;
    private final JWTSigner jwtSigner;
    private final Key key;

    private String[] permitAllUrlPatterns() {
        return new String[] {
                "/auth/logout", "/auth/join", "/auth/email", "/auth/find/id",
                "/auth/reset/password", "/static/**", "/*.html", "/oauth2/fcm",
                "/oauth2/image", "/auth/join/name",
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((requests) ->
                        requests.requestMatchers(permitAllUrlPatterns()).permitAll()
                                .anyRequest().authenticated())
                .exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint));

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.userDetailsService(userDetailsService);

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(http, jwtSigner, memberService, refreshTokenService);
        jwtAuthenticationFilter.setAuthenticationFailureHandler(authFailureHandler);
        jwtAuthenticationFilter.setFilterProcessesUrl("/auth/login");

        JwtKakaoAuthenticationFilter jwtKakaoAuthenticationFilter =
                new JwtKakaoAuthenticationFilter(http, jwtSigner, memberService, refreshTokenService);
        jwtKakaoAuthenticationFilter.setAuthenticationFailureHandler(authFailureHandler);
        jwtKakaoAuthenticationFilter.setFilterProcessesUrl("/auth/kakao");

        http.addFilter(jwtAuthenticationFilter).addFilter(jwtKakaoAuthenticationFilter)
                .addFilterBefore(new JwtAuthorizationRsaFilter(key), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

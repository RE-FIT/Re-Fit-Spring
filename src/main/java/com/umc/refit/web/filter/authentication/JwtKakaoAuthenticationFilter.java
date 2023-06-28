package com.umc.refit.web.filter.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.umc.refit.domain.dto.member.ResLoginDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.member.LoginException;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.signature.SecuritySigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

import static com.umc.refit.exception.ExceptionType.BASIC_MEMBER_EXIST;

@RequiredArgsConstructor
public class JwtKakaoAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final SecuritySigner securitySigner;
    private final JWK jwk;
    private final MemberService memberService;

    //랜덤 문자열
    private String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

    /*카카오 로그인 인증 시작*/
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String KAKAO_API_URL = "https://kapi.kakao.com/v2/user/me";
        String DEFAULT_PASSWORD = "KAKAO_LOGIN";
        String accessToken = request.getHeader("Authorization");

        String EMAIL = getEmailFromKakao(accessToken, KAKAO_API_URL);

        Optional<Member> findMember = memberService.findMemberByEmail(EMAIL);
        if (findMember.isPresent()) { //멤버가 존재할 경우
            if (findMember.get().getSocialType() == (null)) { //일반 로그인일 경우
                request.setAttribute("exception", BASIC_MEMBER_EXIST);
                throw new LoginException(BASIC_MEMBER_EXIST,
                        BASIC_MEMBER_EXIST.getCode(), BASIC_MEMBER_EXIST.getErrorMessage());
            }
        } else {

            /*카카오 로그인 시 유일한 멤버 닉네임 생성*/
            String name;
            while (true) {
                String randomString = generateRandomString(4);
                name = "환경지킴이" + randomString;

                Optional<Member> member = memberService.findMemberByName(name);
                if (member.isEmpty()) {
                    break;
                }
            }

            Member member = new Member(EMAIL, DEFAULT_PASSWORD, name);
            member.getRoles().add("USER");
            memberService.save(member);
        }

        AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(EMAIL, DEFAULT_PASSWORD);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return authentication;
    }

    /*카카오 로그인 인증 성공시 토큰 발행하는 메소드*/
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {
        User user = (User) authResult.getPrincipal();
        try {
            //엑세스 토큰 및 리프레쉬 토큰 발행
            String accessToken = securitySigner.getJwtToken(user, jwk, 216000000);
            String refreshToken = securitySigner.getJwtToken(user, jwk, 216000000);

            //엑세스 토큰 헤더를 통해 전달
            response.addHeader("Authorization", "Bearer " + accessToken); //발행받은 토큰을 response 헤더에 담아 응답

            //리프레쉬 토큰 바디에 담아 전달
            ResLoginDto resEmailDto = new ResLoginDto(refreshToken);
            response.setContentType("application/json");
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(resEmailDto);
            response.getWriter().write(jsonString);

        } catch (JOSEException e) {
            e.printStackTrace();
        }
    }

    /*카카오 인가 서버에 이메일 정보 요청*/
    private String getEmailFromKakao(String accessToken, String KAKAO_API_URL) {
        String email = "";
        try {
            URL url = new URL(KAKAO_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            JsonElement element = JsonParser.parseString(result.toString());
            email = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();
            br.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return email;
    }

    /*랜덤 문자열 생성 메서드*/
    public String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }
}

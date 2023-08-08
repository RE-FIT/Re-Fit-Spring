package com.umc.refit.web.filter.authorization;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.umc.refit.exception.ExceptionType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.umc.refit.exception.ExceptionType.*;

/*Bearer 토큰을 RSA 알고리즘에 의해 검증하며 검증 성공시 인증 및 인가를 처리하는 필터*/
public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private RSAKey jwk;

    public JwtAuthorizationRsaFilter(RSAKey rsaKey) {
        this.jwk = rsaKey;
    }

    /*인가 처리를 거치지 않는 URL 설정 필터*/
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return (pathMatcher.match("/auth/join", path) //일반 회원 가입
                || pathMatcher.match("/auth/kakao", path) //카카오 로그인
                || pathMatcher.match("/auth/email", path) //이메일 인증
                || pathMatcher.match("/auth/find/id", path) //아이디 찾기
                || pathMatcher.match("/auth/reset/password", path) //패스워드 찾기
                || pathMatcher.match("/auth/login", path) //일반 로그인
                || pathMatcher.match("/*.html", path)
//                || pathMatcher.match("/**", path) //API 테스트를 위해 모든 로직에 대해 인가 제외
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        ExceptionType errorType = null;

        /*토큰 헤더 검증*/
        if (tokenResolve(request, response, chain)){
            errorType = TOKEN_NOT_EXIST;
        } else {

            //Bearer를 제거한 토큰 값만 추출(header + payload + signature)
            String token = getToken(request);
            SignedJWT signedJWT;
            try {

                //header와 payload와 signature 값이 속성으로 매핑됨
                signedJWT = SignedJWT.parse(token);
                RSASSAVerifier jwsVerifier = new RSASSAVerifier(jwk.toRSAPublicKey());

                /*토큰 만료기간 검증*/
                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                Date now = new Date();
                if (now.after(expirationTime)) {
                    errorType = TOKEN_EXPIRED;
                } else {

                    boolean verify = signedJWT.verify(jwsVerifier);

                    if (verify) {
                        String username = signedJWT.getJWTClaimsSet().getClaim("id").toString();
                        List<String> authority = (List) signedJWT.getJWTClaimsSet().getClaim("role");

                        //사용자 정보를 만들어서 인증 객체 생성 후 Security Context에 보관
                        if (username != null) {
                            UserDetails user = User.builder().username(username)
                                    .password(UUID.randomUUID().toString())
                                    .authorities(authority.get(0))
                                    .build();
                            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    } else {
                        errorType = TOKEN_INVALID;
                    }
                }
            } catch (Exception e) {
                errorType = TOKEN_INVALID;
            }
        }
        /*토큰 예외 처리*/
        if (errorType != null) {
            request.setAttribute("exception", errorType);
        }
        chain.doFilter(request, response); //다음 필터로 넘어감
    }

    /*Authorization 헤더로 넘어온 엑세스 토큰 값 추출*/
    protected String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization").replace("Bearer ", "");
    }

    /*헤더 유효성 검사*/
    protected boolean tokenResolve(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        return header == null || !header.startsWith("Bearer ");
    }
}

package com.umc.refit.web.filter.authorization;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.umc.refit.exception.ExceptionType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.umc.refit.exception.ExceptionType.*;

public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private RSAKey jwk;

    public JwtAuthorizationRsaFilter(RSAKey rsaKey) {
        this.jwk = rsaKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return (pathMatcher.match("/auth/join", path)
                || pathMatcher.match("/auth/kakao", path)
                || pathMatcher.match("/auth/email", path)
                || pathMatcher.match("/auth/find/id", path)
                || pathMatcher.match("/auth/reset/password", path)
                || pathMatcher.match("/auth/login", path)
                || pathMatcher.match("/*.html", path)
                || pathMatcher.match("/oauth2/fcm", path)
                || pathMatcher.match("/oauth2/image", path)
                || pathMatcher.match("/auth/join/name", path)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        ExceptionType errorType = null;

        if (tokenResolve(request, response, chain)){
            errorType = TOKEN_NOT_EXIST;
        } else {

            String token = getToken(request);
            SignedJWT signedJWT;
            try {
                signedJWT = SignedJWT.parse(token);
                RSASSAVerifier jwsVerifier = new RSASSAVerifier(jwk.toRSAPublicKey());

                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                Date now = new Date();
                if (now.after(expirationTime)) {
                    errorType = TOKEN_EXPIRED;
                } else {

                    boolean verify = signedJWT.verify(jwsVerifier);

                    if (verify) {
                        String username = signedJWT.getJWTClaimsSet().getClaim("id").toString();
                        List<String> authority = (List) signedJWT.getJWTClaimsSet().getClaim("role");

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
        if (errorType != null) {
            request.setAttribute("exception", errorType);
        }
        chain.doFilter(request, response);
    }

    protected String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization").replace("Bearer ", "");
    }

    protected boolean tokenResolve(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        return header == null || !header.startsWith("Bearer ");
    }
}

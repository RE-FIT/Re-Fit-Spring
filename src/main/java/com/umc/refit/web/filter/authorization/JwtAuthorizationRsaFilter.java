package com.umc.refit.web.filter.authorization;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.umc.refit.exception.ExceptionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.umc.refit.exception.ExceptionType.*;

@RequiredArgsConstructor
public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private final Key key;

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
        try {
            if (tokenResolve(request)) {
                setException(request, TOKEN_NOT_EXIST, chain, response);
                return;
            }

            String token = getToken(request);
            if (token == null) {
                setException(request, TOKEN_INVALID, chain, response);
                return;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (isTokenExpired(claims)) {
                setException(request, TOKEN_EXPIRED, chain, response);
                return;
            }

            createAuthentication(claims);
        } catch (Exception e) {
            setException(request, TOKEN_INVALID, chain, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void setException(HttpServletRequest request, ExceptionType errorType, FilterChain chain, HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("exception", errorType);
        chain.doFilter(request, response);
    }

    private boolean isTokenExpired(Claims claims) {
        return new Date().after(claims.getExpiration());
    }

    private void createAuthentication(Claims claims) {
        String username = claims.getSubject();
        List<String> authority = (List<String>) claims.get("role");
        if (username != null && !authority.isEmpty()) {
            UserDetails user = User.builder().username(username)
                    .password(UUID.randomUUID().toString())
                    .authorities(authority.get(0))
                    .build();
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    protected String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization").replace("Bearer ", "");
    }

    protected boolean tokenResolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header == null || !header.startsWith("Bearer ");
    }
}

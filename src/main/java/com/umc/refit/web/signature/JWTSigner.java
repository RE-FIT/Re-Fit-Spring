package com.umc.refit.web.signature;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JWTSigner {

    @Value("${token.issuer}")
    private String issuer;
    private final Key key;

    public String getJwtToken(User user, Integer time) {
        List<String> authority = user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setExpiration(new Date(new Date().getTime() + time))
                .claim("role", authority)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

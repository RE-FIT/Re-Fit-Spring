package com.umc.refit.web.signature;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SecuritySigner {

    @Value("${token.issuer}")
    private String issuer;

    /*토큰 발행 메서드*/
    public String getJwtTokenInternal(JWSSigner jwsSigner, UserDetails user, JWK jwk, Integer time) throws JOSEException {

        JWSHeader header= new JWSHeader.Builder((JWSAlgorithm) jwk.getAlgorithm()).keyID(jwk.getKeyID()).build();

        List<String> authority = user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .claim("id", user.getUsername())
                .claim("role", authority)
                .expirationTime(new Date(new Date().getTime() + time))
                .build();

        //최종 서명 객체가 SignedJWT 객체
        SignedJWT signedJWT = new SignedJWT(header, jwtClaimsSet);
        signedJWT.sign(jwsSigner);
        String jwtToken = signedJWT.serialize();

        //서명에 성공하면 JWT 토큰 발행
        return jwtToken;
    }

    //UserDetails가 UserDetailsService 에서 반환된 User 객체
    public abstract String getJwtToken(UserDetails user, JWK jwk, Integer time) throws JOSEException;
}

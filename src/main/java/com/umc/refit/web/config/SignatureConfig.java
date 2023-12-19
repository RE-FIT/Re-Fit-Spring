package com.umc.refit.web.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.umc.refit.web.signature.RSASecuritySigner;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;

@Configuration
public class SignatureConfig {

    @Bean
    public RSASecuritySigner rsaSecuritySigner() {
        return new RSASecuritySigner();
    }

    @Bean
    public RSAKey rsaKey512() throws JOSEException {
        return new RSAKeyGenerator(2048)
                .keyID("rsaKey")
                .algorithm(JWSAlgorithm.RS512)
                .generate();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Key jwtSigningKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
}
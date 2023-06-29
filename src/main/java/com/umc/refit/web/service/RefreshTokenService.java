package com.umc.refit.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    /*리프레쉬 토큰 저장*/
    public void saveRefreshToken(String username, String refreshToken) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(username, refreshToken, 7, TimeUnit.DAYS);
    }

    /*저장된 리프레쉬 토큰 조회*/
    public String findRefreshTokenByUsername(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    /*저장된 리프레쉬 토큰 삭제*/
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(username);
    }
}
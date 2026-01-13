package com.prepaid.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String username, String refreshToken, long expirationMs) {
        redisTemplate.opsForValue().set("RT:" + username, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("RT:" + username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete("RT:" + username);
    }

    public void addToBlacklist(String accessToken, long remainingTimeMs) {
        if (remainingTimeMs > 0) {
            redisTemplate.opsForValue().set("BL:" + accessToken, "logout", remainingTimeMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey("BL:" + accessToken);
    }
}

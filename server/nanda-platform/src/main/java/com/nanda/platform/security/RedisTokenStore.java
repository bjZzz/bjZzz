package com.nanda.platform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.redis", name = "enabled", havingValue = "true")
public class RedisTokenStore implements TokenStore {

    private static final String REFRESH_PREFIX = "nanda:refresh:";
    private static final String BLACKLIST_PREFIX = "nanda:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void storeRefreshToken(Long userId, String tokenId, String refreshToken, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(refreshKey(userId, tokenId), refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> getRefreshToken(Long userId, String tokenId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(refreshKey(userId, tokenId)));
    }

    @Override
    public void removeRefreshToken(Long userId, String tokenId) {
        stringRedisTemplate.delete(refreshKey(userId, tokenId));
    }

    @Override
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String jti) {
        Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti);
        return exists != null && exists;
    }

    private String refreshKey(Long userId, String tokenId) {
        return REFRESH_PREFIX + userId + ":" + tokenId;
    }
}

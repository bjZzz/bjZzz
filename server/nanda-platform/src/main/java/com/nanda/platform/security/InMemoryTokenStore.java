package com.nanda.platform.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "nanda.redis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, String> refreshTokens = new ConcurrentHashMap<String, String>();
    private final Map<String, Long> blacklist = new ConcurrentHashMap<String, Long>();

    @Override
    public void storeRefreshToken(Long userId, String tokenId, String refreshToken, long ttlSeconds) {
        refreshTokens.put(refreshKey(userId, tokenId), refreshToken);
    }

    @Override
    public Optional<String> getRefreshToken(Long userId, String tokenId) {
        return Optional.ofNullable(refreshTokens.get(refreshKey(userId, tokenId)));
    }

    @Override
    public void removeRefreshToken(Long userId, String tokenId) {
        refreshTokens.remove(refreshKey(userId, tokenId));
    }

    @Override
    public void blacklistAccessToken(String jti, long ttlSeconds) {
        blacklist.put(jti, System.currentTimeMillis() + ttlSeconds * 1000L);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String jti) {
        Long expireAt = blacklist.get(jti);
        if (expireAt == null) {
            return false;
        }
        if (expireAt < System.currentTimeMillis()) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    private String refreshKey(Long userId, String tokenId) {
        return userId + ":" + tokenId;
    }
}

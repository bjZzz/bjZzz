package com.nanda.platform.security;

import java.util.Optional;

public interface TokenStore {

    void storeRefreshToken(Long userId, String tokenId, String refreshToken, long ttlSeconds);

    Optional<String> getRefreshToken(Long userId, String tokenId);

    void removeRefreshToken(Long userId, String tokenId);

    void blacklistAccessToken(String jti, long ttlSeconds);

    boolean isAccessTokenBlacklisted(String jti);
}

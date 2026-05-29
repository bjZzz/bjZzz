package com.nanda.platform.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.audit.AuditLog;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.security.jwt.JwtProperties;
import com.nanda.common.security.jwt.JwtUtils;
import com.nanda.platform.auth.domain.dto.LoginRequest;
import com.nanda.platform.auth.domain.dto.LoginResponse;
import com.nanda.platform.auth.domain.dto.RefreshTokenRequest;
import com.nanda.platform.auth.domain.dto.RefreshTokenResponse;
import com.nanda.platform.security.TokenStore;
import com.nanda.platform.user.domain.entity.SysUser;
import com.nanda.platform.user.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final TokenStore tokenStore;

    @AuditLog(action = "LOGIN", resourceType = "auth")
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_BAD_CREDENTIALS, "用户名或密码错误");
        }
        if ("FROZEN".equals(user.getStatus()) || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_FROZEN, "账号已冻结或禁用");
        }
        Long orgId = user.getPrimaryOrgId() != null ? user.getPrimaryOrgId() : user.getOrgId();
        List<String> permissions = sysUserMapper.selectPermCodesByUserId(user.getId());

        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername(), orgId);
        String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername(), orgId, tokenId);
        tokenStore.storeRefreshToken(user.getId(), tokenId, refreshToken, jwtProperties.getRefreshExpireSeconds());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtProperties.getAccessExpireSeconds());
        response.setPermissions(permissions);

        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setDisplayName(user.getDisplayName());
        info.setOrgId(orgId);
        response.setUser(info);
        return response;
    }

    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        Claims claims = jwtUtils.parseToken(request.getRefreshToken());
        if (!jwtUtils.isRefreshToken(claims)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }
        Long userId = claims.get("userId", Long.class);
        String tokenId = jwtUtils.getJti(claims);
        String stored = tokenStore.getRefreshToken(userId, tokenId).orElse(null);
        if (stored == null || !stored.equals(request.getRefreshToken())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌已失效");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || "FROZEN".equals(user.getStatus()) || "DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_FROZEN, "账号已冻结或禁用");
        }
        Long orgId = claims.get("orgId", Long.class);
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername(), orgId));
        response.setExpiresIn(jwtProperties.getAccessExpireSeconds());
        return response;
    }

    @AuditLog(action = "LOGOUT", resourceType = "auth")
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return;
        }
        try {
            Claims claims = jwtUtils.parseToken(accessToken);
            String jti = jwtUtils.getJti(claims);
            long ttl = jwtProperties.getAccessExpireSeconds();
            if (claims.getExpiration() != null) {
                long remain = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
                if (remain > 0) {
                    ttl = remain;
                }
            }
            tokenStore.blacklistAccessToken(jti, ttl);
            if (jwtUtils.isRefreshToken(claims)) {
                Long userId = claims.get("userId", Long.class);
                tokenStore.removeRefreshToken(userId, jti);
            }
        } catch (Exception ignored) {
            // ignore invalid token on logout
        }
    }

    public RefreshTokenResponse refreshFromContext(AuthContext ctx) {
        if (ctx == null || ctx.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或令牌无效");
        }
        SysUser user = sysUserMapper.selectById(ctx.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        Long orgId = ctx.getOrgId() != null ? ctx.getOrgId()
                : (user.getPrimaryOrgId() != null ? user.getPrimaryOrgId() : user.getOrgId());
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setAccessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername(), orgId));
        response.setExpiresIn(jwtProperties.getAccessExpireSeconds());
        return response;
    }
}

package com.nanda.platform.auth.controller;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.core.result.Result;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.platform.auth.domain.dto.LoginRequest;
import com.nanda.platform.auth.domain.dto.LoginResponse;
import com.nanda.platform.auth.domain.dto.RefreshTokenRequest;
import com.nanda.platform.auth.domain.dto.RefreshTokenResponse;
import com.nanda.platform.auth.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "认证")
@RestController
@RequestMapping(CommonConstants.API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @ApiOperation("刷新访问令牌")
    @PostMapping("/refresh")
    public Result<RefreshTokenResponse> refresh(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            return Result.ok(authService.refresh(request));
        }
        return Result.ok(authService.refreshFromContext(AuthContextHolder.get()));
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = resolveBearerToken(request);
        authService.logout(token);
        return Result.ok(null);
    }

    @ApiOperation("当前用户")
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        AuthContext ctx = AuthContextHolder.get();
        Map<String, Object> data = new HashMap<String, Object>();
        if (ctx != null) {
            data.put("userId", ctx.getUserId());
            data.put("username", ctx.getUsername());
            data.put("orgId", ctx.getOrgId());
            data.put("orgIds", ctx.getOrgIds());
            data.put("permissions", ctx.getPermissions());
        }
        return Result.ok(data);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

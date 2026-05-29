package com.nanda.platform.security;

import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.security.jwt.JwtUtils;
import com.nanda.platform.user.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final TokenStore tokenStore;
    private final DataPermissionService dataPermissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Claims claims = jwtUtils.parseToken(token);
                    if (!jwtUtils.isRefreshToken(claims) && !tokenStore.isAccessTokenBlacklisted(jwtUtils.getJti(claims))) {
                        Long userId = claims.get("userId", Long.class);
                        String username = claims.get("username", String.class);
                        Long orgId = claims.get("orgId", Long.class);

                        List<String> perms = sysUserMapper.selectPermCodesByUserId(userId);
                        Set<Long> accessibleOrgIds = dataPermissionService.resolveAccessibleOrgIds(userId, orgId);

                        AuthContext ctx = new AuthContext();
                        ctx.setUserId(userId);
                        ctx.setUsername(username);
                        ctx.setOrgId(orgId);
                        ctx.setOrgIds(accessibleOrgIds);
                        ctx.setPermissions(new HashSet<String>(perms));
                        AuthContextHolder.set(ctx);

                        List<SimpleGrantedAuthority> authorities = perms.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception ignored) {
                    // invalid token — proceed without authentication
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthContextHolder.clear();
        }
    }
}

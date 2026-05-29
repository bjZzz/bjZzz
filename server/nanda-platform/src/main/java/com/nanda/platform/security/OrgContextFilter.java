package com.nanda.platform.security;

import com.nanda.common.core.constant.CommonConstants;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OrgContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String orgHeader = request.getHeader(CommonConstants.HEADER_ORG_ID);
        AuthContext ctx = AuthContextHolder.get();
        if (ctx != null && StringUtils.hasText(orgHeader)) {
            try {
                Long requestedOrgId = Long.parseLong(orgHeader);
                if (ctx.getOrgIds().contains(requestedOrgId)) {
                    ctx.setOrgId(requestedOrgId);
                }
            } catch (NumberFormatException ignored) {
                // keep token orgId
            }
        }
        filterChain.doFilter(request, response);
    }
}

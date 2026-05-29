package com.nanda.integration.service;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;

public final class IntegrationOrgContext {

    private IntegrationOrgContext() {
    }

    public static Long requireOrgId() {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return ctx.getOrgId();
    }

    public static Long resolveOrgId(Long requestedOrgId) {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            if (requestedOrgId != null) {
                return requestedOrgId;
            }
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        if (requestedOrgId == null || requestedOrgId.equals(ctx.getOrgId())) {
            return ctx.getOrgId();
        }
        if (ctx.getOrgIds().contains(requestedOrgId)) {
            return requestedOrgId;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该机构");
    }

    public static boolean hasPermission(String permission) {
        AuthContext ctx = AuthContextHolder.get();
        return ctx != null && ctx.getPermissions().contains(permission);
    }
}

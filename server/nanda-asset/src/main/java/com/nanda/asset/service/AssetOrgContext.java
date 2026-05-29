package com.nanda.asset.service;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;

public final class AssetOrgContext {

    private AssetOrgContext() {
    }

    public static Long requireOrgId() {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return ctx.getOrgId();
    }

    public static Long currentUserId() {
        AuthContext ctx = AuthContextHolder.get();
        return ctx != null ? ctx.getUserId() : null;
    }
}

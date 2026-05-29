package com.nanda.common.security.aspect;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.annotation.RequiresPermission;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class RequiresPermissionAspect {

    @Before("@annotation(requiresPermission)")
    public void checkPermission(RequiresPermission requiresPermission) {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || !ctx.getPermissions().contains(requiresPermission.value())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无操作权限");
        }
    }
}

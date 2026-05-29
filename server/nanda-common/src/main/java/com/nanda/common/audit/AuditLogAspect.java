package com.nanda.common.audit;

import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired(required = false)
    private AuditLogWriter auditLogWriter;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        Object result = pjp.proceed();
        if (auditLogWriter != null) {
            AuthContext ctx = AuthContextHolder.get();
            auditLogWriter.write(
                    auditLog.action(),
                    auditLog.resourceType(),
                    "",
                    null,
                    ctx != null ? ctx.getUserId() : null,
                    ctx != null ? ctx.getOrgId() : null,
                    resolveClientIp());
        }
        return result;
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.nanda.common.audit;

public interface AuditLogWriter {

    void write(String action, String resourceType, String resourceId, String detailJson,
               Long userId, Long orgId, String ip);
}

package com.nanda.platform.audit.service;

import com.nanda.common.audit.AuditLogWriter;
import com.nanda.common.util.IdGenerator;
import com.nanda.platform.audit.domain.entity.SysAuditLog;
import com.nanda.platform.audit.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysAuditLogWriter implements AuditLogWriter {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Override
    public void write(String action, String resourceType, String resourceId, String detailJson,
                      Long userId, Long orgId, String ip) {
        SysAuditLog log = new SysAuditLog();
        log.setId(IdGenerator.nextId());
        log.setUserId(userId);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setDetailJson(detailJson);
        log.setIp(ip);
        log.setOrgId(orgId);
        log.setCreatedAt(LocalDateTime.now());
        sysAuditLogMapper.insert(log);
    }
}

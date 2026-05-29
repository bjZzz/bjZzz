package com.nanda.platform.audit.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogVO {

    private Long id;
    private Long userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String detailJson;
    private String ip;
    private Long orgId;
    private LocalDateTime createdAt;
}

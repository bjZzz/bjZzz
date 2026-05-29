package com.nanda.platform.audit.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId
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

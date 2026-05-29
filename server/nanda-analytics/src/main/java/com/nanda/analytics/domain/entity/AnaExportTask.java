package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_export_task")
public class AnaExportTask {

    @TableId
    private Long id;
    private Long searchQueryId;
    private String exportFormat;
    private String exportScopeJson;
    private String status;
    private Long approverId;
    private LocalDateTime approvedAt;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;
}

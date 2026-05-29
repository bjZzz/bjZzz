package com.nanda.ingestion.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SyncJobVO {

    private Long id;
    private Long sourceId;
    private String scheduleType;
    private String cronExpr;
    private LocalDateTime lastRunAt;
    private String lastStatus;
    private Long orgId;
    private LocalDateTime createdAt;
}

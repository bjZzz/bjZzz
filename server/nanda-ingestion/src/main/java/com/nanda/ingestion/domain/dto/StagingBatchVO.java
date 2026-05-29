package com.nanda.ingestion.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StagingBatchVO {

    private Long id;
    private Long sourceId;
    private Long jobId;
    private Long orgId;
    private LocalDateTime receivedAt;
    private Integer recordCount;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}

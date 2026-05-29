package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stg_batch")
public class StgBatch {

    @TableId
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

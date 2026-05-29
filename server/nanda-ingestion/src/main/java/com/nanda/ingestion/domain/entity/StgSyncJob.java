package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stg_sync_job")
public class StgSyncJob {

    @TableId
    private Long id;
    private Long sourceId;
    private String scheduleType;
    private String cronExpr;
    private LocalDateTime lastRunAt;
    private String lastStatus;
    private Long orgId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

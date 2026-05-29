package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stg_sync_log")
public class StgSyncLog {

    @TableId
    private Long id;
    private Long jobId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String status;
    private String message;
    private Long orgId;
}

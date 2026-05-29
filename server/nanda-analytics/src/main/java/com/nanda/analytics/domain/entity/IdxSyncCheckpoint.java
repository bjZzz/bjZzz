package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("idx_sync_checkpoint")
public class IdxSyncCheckpoint {

    @TableId
    private Long id;
    private Long orgId;
    private LocalDateTime lastSyncAt;
    private String checkpointJson;
}

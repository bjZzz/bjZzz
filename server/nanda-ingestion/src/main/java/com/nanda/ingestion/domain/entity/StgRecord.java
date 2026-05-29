package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("stg_record")
public class StgRecord {

    @TableId
    private Long id;
    private Long batchId;
    private String domain;
    private String rawPayload;
    private String sourceRef;
    private String parseStatus;
    private String parseError;
    private Long orgId;
}

package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qc_sample_batch")
public class QcSampleBatch {

    @TableId
    private Long id;
    private String batchName;
    private String strategyJson;
    private Long orgId;
    private LocalDateTime createdAt;
}

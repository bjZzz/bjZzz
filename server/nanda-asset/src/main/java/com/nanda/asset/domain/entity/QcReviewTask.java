package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qc_review_task")
public class QcReviewTask {

    @TableId
    private Long id;
    private Long sampleRecordId;
    private String status;
    private Long reviewerId;
    private Long orgId;
    private LocalDateTime createdAt;
}

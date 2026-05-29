package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_publish_task")
public class GovPublishTask {

    @TableId
    private Long id;
    private Long batchId;
    private Long ruleId;
    private String status;
    private Long orgId;
    private LocalDateTime createdAt;
}

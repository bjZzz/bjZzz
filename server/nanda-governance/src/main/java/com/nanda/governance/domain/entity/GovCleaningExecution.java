package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_cleaning_execution")
public class GovCleaningExecution {

    @TableId
    private Long id;
    private Long batchId;
    private Long ruleId;
    private String status;
    private String resultJson;
    private Long orgId;
    private LocalDateTime createdAt;
}

package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_cleaning_rule")
public class GovCleaningRule {

    @TableId
    private Long id;
    private String ruleCode;
    private String ruleType;
    private String ruleConfigJson;
    private String specialtyType;
    private String status;
    private Long orgId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

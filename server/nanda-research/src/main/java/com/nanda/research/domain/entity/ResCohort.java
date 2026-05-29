package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("res_cohort")
public class ResCohort {

    @TableId
    private Long id;
    private Long projectId;
    private String cohortName;
    private String cohortType;
    private String ruleJson;
    private Integer memberCount;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

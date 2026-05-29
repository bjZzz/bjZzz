package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("res_follow_up_plan")
public class ResFollowUpPlan {

    @TableId
    private Long id;
    private Long projectId;
    private String planName;
    private Long orgId;
    private LocalDateTime createdAt;
}

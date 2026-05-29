package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("res_follow_up_stage")
public class ResFollowUpStage {

    @TableId
    private Long id;
    private Long planId;
    private String stageName;
    private Integer offsetDays;
    private Integer windowDays;
    private Integer sortOrder;
}

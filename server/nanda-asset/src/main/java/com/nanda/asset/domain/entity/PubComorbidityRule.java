package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pub_comorbidity_rule")
public class PubComorbidityRule {

    @TableId
    private Long id;
    private String ruleName;
    private String expressionJson;
    private String timeWindowJson;
    private String status;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}

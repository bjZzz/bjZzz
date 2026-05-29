package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("empi_match_rule")
public class EmpiMatchRule {

    @TableId
    private Long id;
    private String ruleName;
    private String ruleConfigJson;
    private String status;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}

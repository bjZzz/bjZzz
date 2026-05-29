package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gov_publish_rule")
public class GovPublishRule {

    @TableId
    private Long id;
    private String ruleName;
    private String specialtyType;
    private String inclusionJson;
    private Long fieldMappingId;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}

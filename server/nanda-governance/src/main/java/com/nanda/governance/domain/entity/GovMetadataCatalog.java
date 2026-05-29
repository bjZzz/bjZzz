package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gov_metadata_catalog")
public class GovMetadataCatalog {

    @TableId
    private Long id;
    private String catalogCode;
    private String catalogName;
    private Long parentId;
    private Long orgId;

    @TableLogic
    private Integer deleted;
}

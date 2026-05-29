package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_metadata_lineage_edge")
public class GovMetadataLineageEdge {

    @TableId
    private Long id;
    private String sourceType;
    private String sourceId;
    private String targetType;
    private String targetId;
    private Long orgId;
    private LocalDateTime createdAt;
}

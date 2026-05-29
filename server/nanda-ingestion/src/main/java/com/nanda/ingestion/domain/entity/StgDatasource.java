package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stg_datasource")
public class StgDatasource {

    @TableId
    private Long id;
    private String sourceCode;
    private String sourceName;
    private String protocol;
    private String configJson;
    private Long orgId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

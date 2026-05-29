package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_crf_form")
public class GovCrfForm {

    @TableId
    private Long id;
    private String formCode;
    private String formName;
    private String specialtyType;
    private Integer version;
    private String schemaJson;
    private String scoreRulesJson;
    private String status;
    private LocalDateTime publishedAt;
    private Long orgId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

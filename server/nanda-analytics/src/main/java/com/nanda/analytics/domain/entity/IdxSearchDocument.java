package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("idx_search_document")
public class IdxSearchDocument {

    @TableId
    private Long id;
    private Long empiId;
    private Long orgId;
    private String specialtyTypes;
    private String diagnosisCodes;
    private String labValues;
    private String demographics;
    private BigDecimal completenessScore;
    private LocalDateTime updatedAt;
}

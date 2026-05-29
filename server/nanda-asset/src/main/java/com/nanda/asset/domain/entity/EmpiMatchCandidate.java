package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("empi_match_candidate")
public class EmpiMatchCandidate {

    @TableId
    private Long id;
    private Long sourceRecordId;
    private Long candidateEmpiId;
    private BigDecimal matchScore;
    private String matchFeatures;
    private String reviewStatus;
    private Long reviewerId;
    private LocalDateTime createdAt;
}

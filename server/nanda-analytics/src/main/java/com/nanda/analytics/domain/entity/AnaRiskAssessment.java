package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_risk_assessment")
public class AnaRiskAssessment {

    @TableId
    private Long id;
    private Long empiId;
    private String modelCode;
    private String inputJson;
    private String resultJson;
    private String riskLevel;
    private LocalDateTime assessedAt;
    private Long orgId;
}

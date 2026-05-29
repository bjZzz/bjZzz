package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qc_metric_snapshot")
public class QcMetricSnapshot {

    @TableId
    private Long id;
    private String metricType;
    private BigDecimal metricValue;
    private LocalDateTime snapshotAt;
    private Long orgId;
}

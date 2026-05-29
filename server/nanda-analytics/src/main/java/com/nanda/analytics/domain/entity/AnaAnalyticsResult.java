package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_analytics_result")
public class AnaAnalyticsResult {

    @TableId
    private Long id;
    private Long jobId;
    private String resultJson;
    private String chartRefs;
    private LocalDateTime createdAt;
}

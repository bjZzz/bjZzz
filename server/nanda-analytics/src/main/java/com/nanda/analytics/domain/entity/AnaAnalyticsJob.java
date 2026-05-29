package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_analytics_job")
public class AnaAnalyticsJob {

    @TableId
    private Long id;
    private String jobType;
    private String methodCode;
    private String inputJson;
    private String status;
    private String sandboxJobId;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;
}

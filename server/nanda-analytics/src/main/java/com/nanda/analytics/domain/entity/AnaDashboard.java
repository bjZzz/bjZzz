package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_dashboard")
public class AnaDashboard {

    @TableId
    private Long id;
    private String dashboardName;
    private String configJson;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;
}

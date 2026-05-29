package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_report")
public class AnaReport {

    @TableId
    private Long id;
    private String reportType;
    private Long sourceId;
    private String fileRef;
    private String status;
    private Long userId;
    private Long orgId;
    private LocalDateTime createdAt;
}

package com.nanda.analytics.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ana_sandbox_dataset")
public class AnaSandboxDataset {

    @TableId
    private Long id;
    private String datasetId;
    private Long orgId;
    private String sourceType;
    private String minioPath;
    private Integer rowCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}

package com.nanda.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("int_upload_batch")
public class IntUploadBatch {

    @TableId
    private Long id;
    private String templateType;
    private String fileName;
    private String fileRef;
    private Long stgBatchId;
    private Long orgId;
    private Integer totalRows;
    private Integer successRows;
    private Integer failRows;
    private String status;
    private String clientRequestId;
    private LocalDateTime createdAt;
}

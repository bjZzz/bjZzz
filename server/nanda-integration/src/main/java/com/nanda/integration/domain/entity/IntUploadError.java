package com.nanda.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("int_upload_error")
public class IntUploadError {

    @TableId
    private Long id;
    private Long uploadBatchId;
    private Integer rowNum;
    private String errorMessage;
    private String rowDataJson;
}

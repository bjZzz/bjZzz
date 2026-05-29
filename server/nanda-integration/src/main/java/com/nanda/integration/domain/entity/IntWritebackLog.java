package com.nanda.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("int_writeback_log")
public class IntWritebackLog {

    @TableId
    private Long id;
    private Long endpointId;
    private String clientRequestId;
    private String payloadJson;
    private Integer responseStatus;
    private String responseBody;
    private Integer retryCount;
    private String status;
    private Long orgId;
    private LocalDateTime createdAt;
}

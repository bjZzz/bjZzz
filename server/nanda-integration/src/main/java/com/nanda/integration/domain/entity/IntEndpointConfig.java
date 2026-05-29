package com.nanda.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("int_endpoint_config")
public class IntEndpointConfig {

    @TableId
    private Long id;
    private String endpointCode;
    private String endpointType;
    private String baseUrl;
    private String authType;
    private String authConfigJson;
    private Long orgId;
    private String status;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

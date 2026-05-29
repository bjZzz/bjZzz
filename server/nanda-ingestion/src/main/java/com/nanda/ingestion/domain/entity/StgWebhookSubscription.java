package com.nanda.ingestion.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stg_webhook_subscription")
public class StgWebhookSubscription {

    @TableId
    private Long id;
    private String endpointUrl;
    private String secretHash;
    private Long orgId;
    private String status;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

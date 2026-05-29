package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pub_data_change_log")
public class PubDataChangeLog {

    @TableId
    private Long id;
    private Long patientId;
    private String changeType;
    private String beforeJson;
    private String afterJson;
    private Long operatorId;
    private Long orgId;
    private LocalDateTime createdAt;
}

package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pub_specialty_follow_up")
public class PubSpecialtyFollowUp {

    @TableId
    private Long id;
    private Long patientId;
    private String followUpJson;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

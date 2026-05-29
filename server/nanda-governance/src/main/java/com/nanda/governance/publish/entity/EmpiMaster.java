package com.nanda.governance.publish.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("empi_master")
public class EmpiMaster {

    @TableId
    private Long id;
    private String displayName;
    private String gender;
    private java.time.LocalDate birthDate;
    private String mergeStatus;
    private Long mergedToId;
    private java.math.BigDecimal matchConfidence;
    private Long orgId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

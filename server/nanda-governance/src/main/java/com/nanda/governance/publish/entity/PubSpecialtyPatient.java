package com.nanda.governance.publish.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pub_specialty_patient")
public class PubSpecialtyPatient {

    @TableId
    private Long id;
    private Long empiId;
    private String specialtyType;
    private Long orgId;
    private Long templateId;
    private String coreFields;
    private String extendedFields;
    private String status;
    private java.time.LocalDate firstDiagnosisDate;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

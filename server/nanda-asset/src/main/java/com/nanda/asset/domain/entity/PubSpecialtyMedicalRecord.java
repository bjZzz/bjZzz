package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pub_specialty_medical_record")
public class PubSpecialtyMedicalRecord {

    @TableId
    private Long id;
    private Long patientId;
    private String recordType;
    private String contentJson;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

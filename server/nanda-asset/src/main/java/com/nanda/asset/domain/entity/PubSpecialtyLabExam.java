package com.nanda.asset.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pub_specialty_lab_exam")
public class PubSpecialtyLabExam {

    @TableId
    private Long id;
    private Long patientId;
    private String examCode;
    private String examValue;
    private String examUnit;
    private LocalDate examDate;
    private Long orgId;
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}

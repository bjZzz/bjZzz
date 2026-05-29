package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("res_cohort_member")
public class ResCohortMember {

    @TableId
    private Long id;
    private Long cohortId;
    private Long empiId;
    private String groupLabel;
    private LocalDate enrollDate;
    private String status;
}

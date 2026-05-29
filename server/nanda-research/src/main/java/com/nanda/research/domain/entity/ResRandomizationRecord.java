package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("res_randomization_record")
public class ResRandomizationRecord {

    @TableId
    private Long id;
    private Long cohortId;
    private Long cohortMemberId;
    private String groupAssigned;
    private LocalDateTime randomizedAt;
}

package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("res_follow_up_task")
public class ResFollowUpTask {

    @TableId
    private Long id;
    private Long stageId;
    private Long cohortMemberId;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime completedAt;
    private String channel;
}

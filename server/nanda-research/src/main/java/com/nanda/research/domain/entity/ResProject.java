package com.nanda.research.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("res_project")
public class ResProject {

    @TableId
    private Long id;
    private String projectCode;
    private String projectName;
    private String status;
    private String designJson;
    private String templateCode;
    private Long piUserId;
    private Long orgId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime archivedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}

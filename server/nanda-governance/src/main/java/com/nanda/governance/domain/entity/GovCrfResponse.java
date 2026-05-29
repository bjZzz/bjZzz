package com.nanda.governance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gov_crf_response")
public class GovCrfResponse {

    @TableId
    private Long id;
    private Long formId;
    private Integer formVersion;
    private Long empiId;
    private Long projectId;
    private String answersJson;
    private String scoresJson;
    private String status;
    private Long submittedBy;
    private Long approvedBy;
    private Long orgId;
    private LocalDateTime createdAt;
}

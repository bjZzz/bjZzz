package com.nanda.governance.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrfResponseVO {

    private Long id;
    private Long formId;
    private Integer formVersion;
    private Long empiId;
    private Long projectId;
    private String answersJson;
    private String scoresJson;
    private String status;
    private LocalDateTime createdAt;
}

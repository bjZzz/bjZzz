package com.nanda.ingestion.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataSourceVO {

    private Long id;
    private String sourceCode;
    private String sourceName;
    private String protocol;
    private String configJson;
    private Long orgId;
    private String status;
    private LocalDateTime createdAt;
}

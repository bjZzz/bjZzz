package com.nanda.governance.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrfFormVO {

    private Long id;
    private String formCode;
    private String formName;
    private String specialtyType;
    private Integer version;
    private String schemaJson;
    private String scoreRulesJson;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}

package com.nanda.governance.domain.dto;

import lombok.Data;

@Data
public class PublishRuleVO {

    private Long id;
    private String ruleName;
    private String specialtyType;
    private String inclusionJson;
    private Long fieldMappingId;
}

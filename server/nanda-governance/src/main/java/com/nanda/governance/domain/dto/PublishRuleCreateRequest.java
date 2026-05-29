package com.nanda.governance.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PublishRuleCreateRequest {

    @NotBlank
    private String ruleName;

    @NotBlank
    private String specialtyType;

    @NotBlank
    private String inclusionJson;

    private Long fieldMappingId;
}

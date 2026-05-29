package com.nanda.governance.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CleaningRuleCreateRequest {

    @NotBlank
    private String ruleCode;

    @NotBlank
    private String ruleType;

    @NotBlank
    private String ruleConfigJson;

    private String specialtyType;
}

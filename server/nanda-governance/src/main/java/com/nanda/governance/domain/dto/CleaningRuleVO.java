package com.nanda.governance.domain.dto;

import lombok.Data;

@Data
public class CleaningRuleVO {

    private Long id;
    private String ruleCode;
    private String ruleType;
    private String ruleConfigJson;
    private String specialtyType;
    private String status;
}

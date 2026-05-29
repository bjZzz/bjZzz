package com.nanda.governance.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CrfFormCreateRequest {

    @NotBlank
    private String formCode;

    @NotBlank
    private String formName;

    private String specialtyType;

    @NotBlank
    private String schemaJson;

    private String scoreRulesJson;
}

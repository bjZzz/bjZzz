package com.nanda.governance.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DictDiagnosisCreateRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String nameZh;

    private String nameEn;
}

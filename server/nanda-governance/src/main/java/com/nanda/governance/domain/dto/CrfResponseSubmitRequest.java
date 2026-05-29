package com.nanda.governance.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CrfResponseSubmitRequest {

    @NotNull
    private Long formId;

    private Long empiId;
    private Long projectId;

    @NotBlank
    private String answersJson;
}

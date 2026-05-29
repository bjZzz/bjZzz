package com.nanda.platform.org.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OrgCreateRequest {

    @NotBlank
    private String orgCode;

    @NotBlank
    private String orgName;

    @NotBlank
    private String orgType;

    private Long parentId;
    private String levelType;
}

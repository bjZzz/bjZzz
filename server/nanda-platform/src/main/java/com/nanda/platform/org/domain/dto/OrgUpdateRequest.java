package com.nanda.platform.org.domain.dto;

import lombok.Data;

@Data
public class OrgUpdateRequest {

    private String orgName;
    private String orgType;
    private Long parentId;
    private String levelType;
    private String status;
}

package com.nanda.platform.org.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgVO {

    private Long id;
    private String orgCode;
    private String orgName;
    private String orgType;
    private Long parentId;
    private String levelType;
    private String status;
    private LocalDateTime createdAt;
}

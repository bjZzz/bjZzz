package com.nanda.governance.domain.dto;

import lombok.Data;

@Data
public class LineageEdgeVO {

    private Long id;
    private String sourceType;
    private String sourceId;
    private String targetType;
    private String targetId;
}

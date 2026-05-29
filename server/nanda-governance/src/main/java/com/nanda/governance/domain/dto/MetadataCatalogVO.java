package com.nanda.governance.domain.dto;

import lombok.Data;

@Data
public class MetadataCatalogVO {

    private Long id;
    private String catalogCode;
    private String catalogName;
    private Long parentId;
}

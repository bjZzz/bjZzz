package com.nanda.governance.cleaning;

import lombok.Data;

import java.util.Map;

@Data
public class CleanedRecord {

    private Long recordId;
    private String sourceRef;
    private String domain;
    private Map<String, Object> payload;
    private Long empiId;
    private boolean skipped;
}

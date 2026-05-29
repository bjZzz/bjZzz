package com.nanda.ingestion.adapter;

import lombok.Data;

@Data
public class StagingRecordDTO {

    private String domain;
    private String sourceRef;
    private String rawPayload;
    private String parseStatus;
    private String parseError;
}

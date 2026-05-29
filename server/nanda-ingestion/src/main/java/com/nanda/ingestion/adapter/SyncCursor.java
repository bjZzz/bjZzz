package com.nanda.ingestion.adapter;

import lombok.Data;

@Data
public class SyncCursor {

    private String lastSourceRef;
    private Long lastBatchId;
}

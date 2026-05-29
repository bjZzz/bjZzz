package com.nanda.ingestion.domain.enums;

public final class BatchStatus {

    public static final String RECEIVED = "RECEIVED";
    public static final String CLEANING = "CLEANING";
    public static final String MATCHED = "MATCHED";
    public static final String READY_TO_PUBLISH = "READY_TO_PUBLISH";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String REJECTED = "REJECTED";

    private BatchStatus() {
    }
}

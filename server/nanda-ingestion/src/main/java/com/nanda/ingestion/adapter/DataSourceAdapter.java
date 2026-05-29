package com.nanda.ingestion.adapter;

import java.util.List;

public interface DataSourceAdapter {

    boolean supports(String protocol);

    ConnectionTestResult testConnection(DataSourceConfig config);

    List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor);
}

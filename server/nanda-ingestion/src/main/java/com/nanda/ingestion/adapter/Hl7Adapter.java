package com.nanda.ingestion.adapter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class Hl7Adapter implements DataSourceAdapter {

    @Override
    public boolean supports(String protocol) {
        return "HL7".equalsIgnoreCase(protocol);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceConfig config) {
        return ConnectionTestResult.ok("HL7 适配器就绪（占位）");
    }

    @Override
    public List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor) {
        return Collections.emptyList();
    }
}

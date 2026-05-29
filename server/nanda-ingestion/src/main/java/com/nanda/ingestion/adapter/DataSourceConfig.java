package com.nanda.ingestion.adapter;

import lombok.Data;

import java.util.Map;

@Data
public class DataSourceConfig {

    private String jdbcUrl;
    private String username;
    private String password;
    private String query;
    private String filePath;
    private String fileType;
    private Map<String, Object> extra;
}

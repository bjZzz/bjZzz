package com.nanda.ingestion.adapter;

import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdbcAdapter implements DataSourceAdapter {

    @Override
    public boolean supports(String protocol) {
        return "JDBC".equalsIgnoreCase(protocol);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceConfig config) {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            return ConnectionTestResult.fail("jdbcUrl 未配置");
        }
        try (Connection conn = openConnection(config)) {
            return ConnectionTestResult.ok("JDBC 连接成功");
        } catch (Exception e) {
            return ConnectionTestResult.fail("JDBC 连接失败: " + e.getMessage());
        }
    }

    @Override
    public List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor) {
        String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            query = "SELECT 1 AS id";
        }
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        try (Connection conn = openConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            int rowNum = 0;
            while (rs.next()) {
                rowNum++;
                Map<String, Object> row = new HashMap<String, Object>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                StagingRecordDTO dto = new StagingRecordDTO();
                dto.setDomain("PATIENT");
                dto.setSourceRef("jdbc-row-" + rowNum);
                dto.setRawPayload(JsonUtils.toJson(row));
                dto.setParseStatus("OK");
                records.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("JDBC 拉取失败: " + e.getMessage(), e);
        }
        return records;
    }

    private Connection openConnection(DataSourceConfig config) throws Exception {
        if (config.getUsername() != null) {
            return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
        }
        return DriverManager.getConnection(config.getJdbcUrl());
    }
}

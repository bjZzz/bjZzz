package com.nanda.ingestion.adapter;

import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileAdapter implements DataSourceAdapter {

    @Override
    public boolean supports(String protocol) {
        return "FILE".equalsIgnoreCase(protocol);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceConfig config) {
        if (config.getFilePath() == null || config.getFilePath().isEmpty()) {
            return ConnectionTestResult.fail("filePath 未配置");
        }
        File file = new File(config.getFilePath());
        if (!file.exists() || !file.isFile()) {
            return ConnectionTestResult.fail("文件不存在: " + config.getFilePath());
        }
        return ConnectionTestResult.ok("文件可读");
    }

    @Override
    public List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor) {
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        File file = new File(config.getFilePath());
        String fileType = config.getFileType() != null ? config.getFileType().toUpperCase() : "CSV";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if ("CSV".equals(fileType)) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    return records;
                }
                String[] headers = headerLine.split(",");
                String line;
                int rowNum = 0;
                while ((line = reader.readLine()) != null) {
                    rowNum++;
                    String[] values = line.split(",", -1);
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        row.put(headers[i].trim(), values[i].trim());
                    }
                    StagingRecordDTO dto = new StagingRecordDTO();
                    dto.setDomain("PATIENT");
                    dto.setSourceRef(file.getName() + "#" + rowNum);
                    dto.setRawPayload(JsonUtils.toJson(row));
                    dto.setParseStatus("OK");
                    records.add(dto);
                }
            } else {
                StagingRecordDTO dto = new StagingRecordDTO();
                dto.setDomain("OTHER");
                dto.setSourceRef(file.getName());
                Map<String, Object> payload = new HashMap<String, Object>();
                payload.put("fileName", file.getName());
                payload.put("size", file.length());
                dto.setRawPayload(JsonUtils.toJson(payload));
                dto.setParseStatus("OK");
                records.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败: " + e.getMessage(), e);
        }
        return records;
    }
}

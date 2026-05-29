package com.nanda.ingestion.adapter;

import com.nanda.ingestion.dicom.DicomMetadataParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DicomAdapter implements DataSourceAdapter {

    private final DicomMetadataParser dicomMetadataParser;

    @Override
    public boolean supports(String protocol) {
        return "DICOM".equalsIgnoreCase(protocol);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceConfig config) {
        String path = resolvePath(config);
        if (path == null || path.isEmpty()) {
            return ConnectionTestResult.fail("DICOM 目录 filePath 未配置");
        }
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return ConnectionTestResult.fail("DICOM 目录不存在: " + path);
        }
        return ConnectionTestResult.ok("DICOM 目录可读");
    }

    @Override
    public List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor) {
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        File dir = new File(resolvePath(config));
        File[] files = dir.listFiles();
        if (files == null) {
            return records;
        }
        String lastRef = cursor != null ? cursor.getLastSourceRef() : null;
        boolean afterCursor = lastRef == null;
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".dcm") && !name.endsWith(".dicom")) {
                continue;
            }
            String sourceRef = dicomMetadataParser.resolveSourceRef(
                    dicomMetadataParser.parseFile(file.toPath()));
            if (!afterCursor) {
                if (sourceRef.equals(lastRef)) {
                    afterCursor = true;
                }
                continue;
            }
            records.add(toRecord(file.toPath()));
        }
        return records;
    }

    private StagingRecordDTO toRecord(Path path) {
        Map<String, Object> metadata = dicomMetadataParser.parseFile(path);
        StagingRecordDTO dto = new StagingRecordDTO();
        dto.setDomain("IMAGE");
        dto.setSourceRef(dicomMetadataParser.resolveSourceRef(metadata));
        dto.setRawPayload(dicomMetadataParser.toPayload(metadata));
        dto.setParseStatus("OK");
        return dto;
    }

    private String resolvePath(DataSourceConfig config) {
        if (config.getFilePath() != null && !config.getFilePath().isEmpty()) {
            return config.getFilePath();
        }
        if (config.getExtra() != null && config.getExtra().get("filePath") != null) {
            return String.valueOf(config.getExtra().get("filePath"));
        }
        return null;
    }
}

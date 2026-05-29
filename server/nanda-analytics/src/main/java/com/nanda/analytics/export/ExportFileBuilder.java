package com.nanda.analytics.export;

import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ExportFileBuilder {

    private static final Path EXPORT_DIR = Paths.get("data", "exports");

    public String writeFile(Long taskId, String exportFormat, byte[] content) {
        try {
            Files.createDirectories(EXPORT_DIR);
            String ext = resolveExtension(exportFormat);
            Path file = EXPORT_DIR.resolve(taskId + "." + ext);
            Files.write(file, content);
            return file.toString();
        } catch (IOException ex) {
            log.error("Failed to write export file taskId={}", taskId, ex);
            throw new IllegalStateException("导出文件写入失败", ex);
        }
    }

    public byte[] readFile(String fileRef) {
        try {
            return Files.readAllBytes(Paths.get(fileRef));
        } catch (IOException ex) {
            log.error("Failed to read export file ref={}", fileRef, ex);
            throw new IllegalStateException("导出文件读取失败", ex);
        }
    }

    public String resolveFileName(Long taskId, String exportFormat) {
        return taskId + "." + resolveExtension(exportFormat);
    }

    public String resolveContentType(String exportFormat) {
        if ("CDISC_ODM".equalsIgnoreCase(exportFormat)) {
            return "application/xml";
        }
        if ("JSON".equalsIgnoreCase(exportFormat)) {
            return "application/json";
        }
        return "text/csv";
    }

    public byte[] buildContent(String exportFormat, java.util.List<SearchHitVO> hits, Long taskId, CdiscExportAdapter adapter) {
        if ("CDISC_ODM".equalsIgnoreCase(exportFormat)) {
            return adapter.toOdm(hits, taskId);
        }
        if ("JSON".equalsIgnoreCase(exportFormat)) {
            return adapter.toJson(hits);
        }
        return adapter.toCsv(hits);
    }

    private String resolveExtension(String exportFormat) {
        if ("CDISC_ODM".equalsIgnoreCase(exportFormat)) {
            return "xml";
        }
        if ("JSON".equalsIgnoreCase(exportFormat)) {
            return "json";
        }
        return "csv";
    }
}

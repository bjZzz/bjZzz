package com.nanda.integration.upload;

import com.alibaba.excel.EasyExcel;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelTemplateParser {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;

    public ParsedUpload parse(MultipartFile file, String templateType) {
        validateFile(file, templateType);
        List<Map<Integer, String>> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).sheet().headRowNumber(0).doReadSync();
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "Excel文件读取失败");
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "Excel模板解析失败");
        }

        ParsedUpload parsed = new ParsedUpload();
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "Excel模板不能为空");
        }

        List<String> headers = resolveHeaders(rows.get(0));
        for (int i = 1; i < rows.size(); i++) {
            Map<String, Object> rowData = toNamedRow(headers, rows.get(i));
            if (isBlankRow(rowData)) {
                parsed.addError(i + 1, "空白行", JsonUtils.toJson(rowData));
                continue;
            }
            ParsedRow row = new ParsedRow();
            row.setRowNum(i + 1);
            row.setSourceRef(file.getOriginalFilename() + "#" + (i + 1));
            row.setRawPayload(JsonUtils.toJson(rowData));
            parsed.getRows().add(row);
        }
        parsed.setTotalRows(parsed.getRows().size() + parsed.getErrors().size());
        return parsed;
    }

    private void validateFile(MultipartFile file, String templateType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INTEGRATION_FILE_TOO_LARGE, "上传文件超过50MB");
        }
        if (!StringUtils.hasText(templateType)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "模板类型不能为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)
                || !filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls")) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "仅支持Excel模板文件");
        }
    }

    private List<String> resolveHeaders(Map<Integer, String> headerRow) {
        List<String> headers = new ArrayList<String>();
        if (headerRow == null || headerRow.isEmpty()) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "模板表头不能为空");
        }
        int maxIndex = -1;
        for (Integer index : headerRow.keySet()) {
            if (index != null && index > maxIndex) {
                maxIndex = index;
            }
        }
        for (int i = 0; i <= maxIndex; i++) {
            String header = headerRow.get(i);
            headers.add(StringUtils.hasText(header) ? header.trim() : "column" + i);
        }
        return headers;
    }

    private Map<String, Object> toNamedRow(List<String> headers, Map<Integer, String> row) {
        Map<String, Object> rowData = new LinkedHashMap<String, Object>();
        for (int i = 0; i < headers.size(); i++) {
            String value = row != null ? row.get(i) : null;
            rowData.put(headers.get(i), value);
        }
        return rowData;
    }

    private boolean isBlankRow(Map<String, Object> rowData) {
        for (Object value : rowData.values()) {
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return false;
            }
        }
        return true;
    }

    @Data
    public static class ParsedUpload {
        private int totalRows;
        private List<ParsedRow> rows = new ArrayList<ParsedRow>();
        private List<ParsedError> errors = new ArrayList<ParsedError>();

        public void addError(int rowNum, String message, String rowDataJson) {
            ParsedError error = new ParsedError();
            error.setRowNum(rowNum);
            error.setMessage(message);
            error.setRowDataJson(rowDataJson);
            errors.add(error);
        }
    }

    @Data
    public static class ParsedRow {
        private int rowNum;
        private String sourceRef;
        private String rawPayload;
    }

    @Data
    public static class ParsedError {
        private int rowNum;
        private String message;
        private String rowDataJson;
    }
}

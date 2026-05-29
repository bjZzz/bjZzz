package com.nanda.ingestion.dicom;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.DicomMetadataRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DicomMetadataParser {

    private static final int TAG_PATIENT_ID = 0x00100020;
    private static final int TAG_PATIENT_NAME = 0x00100010;
    private static final int TAG_MODALITY = 0x00080060;
    private static final int TAG_STUDY_UID = 0x0020000D;
    private static final int TAG_SERIES_UID = 0x0020000E;
    private static final int TAG_SOP_UID = 0x00080018;
    private static final int TAG_STUDY_DATE = 0x00080020;

    public Map<String, Object> parseFile(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return parseBytes(bytes, path.getFileName().toString());
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INGESTION_DICOM_PARSE_FAILED, "DICOM 文件读取失败: " + ex.getMessage());
        }
    }

    public Map<String, Object> fromRequest(DicomMetadataRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<String, Object>();
        metadata.put("patientId", request.getPatientId());
        metadata.put("patientName", null);
        metadata.put("modality", request.getModality());
        metadata.put("studyInstanceUid", request.getStudyInstanceUid());
        metadata.put("seriesInstanceUid", request.getSeriesInstanceUid());
        metadata.put("sopInstanceUid", request.getSopInstanceUid());
        metadata.put("studyDate", request.getStudyDate());
        if (request.getExtra() != null) {
            metadata.putAll(request.getExtra());
        }
        validateRequired(metadata);
        return metadata;
    }

    public Map<String, Object> parseBytes(byte[] bytes, String fileName) {
        int offset = findDicmOffset(bytes);
        Map<String, Object> metadata = new LinkedHashMap<String, Object>();
        if (offset >= 0) {
            extractTags(bytes, offset, metadata);
        }
        if (!StringUtils.hasText(stringVal(metadata.get("studyInstanceUid")))) {
            metadata.put("studyInstanceUid", "UNKNOWN-" + fileName);
        }
        if (!StringUtils.hasText(stringVal(metadata.get("patientId")))) {
            metadata.put("patientId", "UNKNOWN");
        }
        metadata.put("fileName", fileName);
        metadata.put("fileSize", bytes.length);
        return metadata;
    }

    public String toPayload(Map<String, Object> metadata) {
        return JsonUtils.toJson(metadata);
    }

    public String resolveSourceRef(Map<String, Object> metadata) {
        String sop = stringVal(metadata.get("sopInstanceUid"));
        if (StringUtils.hasText(sop)) {
            return sop;
        }
        String study = stringVal(metadata.get("studyInstanceUid"));
        return StringUtils.hasText(study) ? study : "DICOM";
    }

    private void validateRequired(Map<String, Object> metadata) {
        if (!StringUtils.hasText(stringVal(metadata.get("studyInstanceUid")))) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "studyInstanceUid 不能为空");
        }
    }

    private int findDicmOffset(byte[] bytes) {
        for (int i = 0; i <= bytes.length - 4; i++) {
            if (bytes[i] == 'D' && bytes[i + 1] == 'I' && bytes[i + 2] == 'C' && bytes[i + 3] == 'M') {
                return i + 4;
            }
        }
        return -1;
    }

    private void extractTags(byte[] bytes, int offset, Map<String, Object> metadata) {
        int pos = offset;
        int guard = 0;
        while (pos + 8 <= bytes.length && guard++ < 256) {
            int group = u16(bytes, pos);
            int element = u16(bytes, pos + 2);
            int tag = (group << 16) | element;
            pos += 4;
            if (pos + 4 > bytes.length) {
                break;
            }
            String vr = new String(bytes, pos, 2, StandardCharsets.US_ASCII);
            pos += 2;
            int length;
            if (isLongVr(vr)) {
                pos += 2;
                if (pos + 4 > bytes.length) {
                    break;
                }
                length = u32(bytes, pos);
                pos += 4;
            } else {
                length = u16(bytes, pos);
                pos += 2;
            }
            if (length < 0 || pos + length > bytes.length) {
                break;
            }
            String value = readValue(bytes, pos, length, vr);
            pos += length;
            applyTag(tag, value, metadata);
            if (metadata.size() >= 7) {
                break;
            }
        }
    }

    private void applyTag(int tag, String value, Map<String, Object> metadata) {
        switch (tag) {
            case TAG_PATIENT_ID:
                metadata.put("patientId", trim(value));
                break;
            case TAG_PATIENT_NAME:
                metadata.put("patientName", trim(value));
                break;
            case TAG_MODALITY:
                metadata.put("modality", trim(value));
                break;
            case TAG_STUDY_UID:
                metadata.put("studyInstanceUid", trim(value));
                break;
            case TAG_SERIES_UID:
                metadata.put("seriesInstanceUid", trim(value));
                break;
            case TAG_SOP_UID:
                metadata.put("sopInstanceUid", trim(value));
                break;
            case TAG_STUDY_DATE:
                metadata.put("studyDate", trim(value));
                break;
            default:
                break;
        }
    }

    private String readValue(byte[] bytes, int pos, int length, String vr) {
        if ("UI".equals(vr) || "LO".equals(vr) || "SH".equals(vr) || "CS".equals(vr) || "PN".equals(vr)) {
            return new String(bytes, pos, length, StandardCharsets.UTF_8).replace("\0", "");
        }
        return new String(bytes, pos, Math.min(length, 64), StandardCharsets.UTF_8).replace("\0", "");
    }

    private boolean isLongVr(String vr) {
        return "OB".equals(vr) || "OW".equals(vr) || "OF".equals(vr) || "SQ".equals(vr) || "UT".equals(vr) || "UN".equals(vr);
    }

    private int u16(byte[] bytes, int pos) {
        return ByteBuffer.wrap(bytes, pos, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF;
    }

    private int u32(byte[] bytes, int pos) {
        return ByteBuffer.wrap(bytes, pos, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

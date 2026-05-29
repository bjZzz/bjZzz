package com.nanda.ingestion.dicom;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.ingestion.adapter.StagingRecordDTO;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.DicomMetadataRequest;
import com.nanda.ingestion.domain.dto.IngestionW8Dtos.DicomUploadResultVO;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.staging.StagingBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DicomIngestService {

    private static final Long DICOM_SOURCE_ID = -1L;

    private final DicomMetadataParser dicomMetadataParser;
    private final StagingBatchService stagingBatchService;

    @Transactional
    public DicomUploadResultVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "DICOM 文件不能为空");
        }
        try {
            Path temp = Files.createTempFile("nanda-dicom-", ".dcm");
            file.transferTo(temp.toFile());
            Map<String, Object> metadata = dicomMetadataParser.parseFile(temp);
            Files.deleteIfExists(temp);
            return ingestMetadata(metadata);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INGESTION_DICOM_PARSE_FAILED, "DICOM 上传解析失败: " + ex.getMessage());
        }
    }

    @Transactional
    public DicomUploadResultVO ingestMetadata(DicomMetadataRequest request) {
        Map<String, Object> metadata = dicomMetadataParser.fromRequest(request);
        return ingestMetadata(metadata);
    }

    private DicomUploadResultVO ingestMetadata(Map<String, Object> metadata) {
        Long orgId = requireOrgId();
        StagingRecordDTO record = new StagingRecordDTO();
        record.setDomain("IMAGE");
        record.setSourceRef(dicomMetadataParser.resolveSourceRef(metadata));
        record.setRawPayload(dicomMetadataParser.toPayload(metadata));
        record.setParseStatus("OK");
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        records.add(record);
        StgBatch batch = stagingBatchService.createBatch(DICOM_SOURCE_ID, null, orgId, records);

        DicomUploadResultVO vo = new DicomUploadResultVO();
        vo.setBatchId(batch.getId());
        vo.setStudyInstanceUid(stringVal(metadata.get("studyInstanceUid")));
        vo.setPatientId(stringVal(metadata.get("patientId")));
        vo.setModality(stringVal(metadata.get("modality")));
        vo.setMetadata(metadata);
        return vo;
    }

    private Long requireOrgId() {
        if (AuthContextHolder.get() == null || AuthContextHolder.get().getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return AuthContextHolder.get().getOrgId();
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

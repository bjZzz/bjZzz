package com.nanda.integration.upload;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.ingestion.adapter.StagingRecordDTO;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.staging.StagingBatchService;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.UploadErrorVO;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.UploadResultVO;
import com.nanda.integration.domain.entity.IntUploadBatch;
import com.nanda.integration.domain.entity.IntUploadError;
import com.nanda.integration.mapper.IntUploadBatchMapper;
import com.nanda.integration.mapper.IntUploadErrorMapper;
import com.nanda.integration.service.IntegrationOrgContext;
import com.nanda.integration.upload.ExcelTemplateParser.ParsedError;
import com.nanda.integration.upload.ExcelTemplateParser.ParsedRow;
import com.nanda.integration.upload.ExcelTemplateParser.ParsedUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UploadBatchService {

    private static final Long UPLOAD_SOURCE_ID = 0L;
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_PARTIAL_FAILED = "PARTIAL_FAILED";
    private static final String STATUS_FAILED = "FAILED";

    private final ExcelTemplateParser excelTemplateParser;
    private final StagingBatchService stagingBatchService;
    private final IntUploadBatchMapper intUploadBatchMapper;
    private final IntUploadErrorMapper intUploadErrorMapper;

    @Transactional
    public UploadResultVO upload(MultipartFile file, String templateType, String clientRequestId) {
        Long orgId = IntegrationOrgContext.requireOrgId();
        if (StringUtils.hasText(clientRequestId)) {
            IntUploadBatch existing = intUploadBatchMapper.selectOne(new LambdaQueryWrapper<IntUploadBatch>()
                    .eq(IntUploadBatch::getOrgId, orgId)
                    .eq(IntUploadBatch::getClientRequestId, clientRequestId)
                    .last("LIMIT 1"));
            if (existing != null) {
                return toVO(existing);
            }
        }

        ParsedUpload parsed = excelTemplateParser.parse(file, templateType);
        IntUploadBatch uploadBatch = new IntUploadBatch();
        uploadBatch.setId(IdGenerator.nextId());
        uploadBatch.setTemplateType(templateType.toUpperCase());
        uploadBatch.setFileName(file.getOriginalFilename());
        uploadBatch.setFileRef("upload://" + orgId + "/" + uploadBatch.getId() + "/" + file.getOriginalFilename());
        uploadBatch.setOrgId(orgId);
        uploadBatch.setClientRequestId(clientRequestId);
        uploadBatch.setTotalRows(parsed.getTotalRows());
        uploadBatch.setSuccessRows(parsed.getRows().size());
        uploadBatch.setFailRows(parsed.getErrors().size());
        uploadBatch.setStatus(resolveStatus(parsed));
        uploadBatch.setCreatedAt(LocalDateTime.now());
        intUploadBatchMapper.insert(uploadBatch);

        for (ParsedError error : parsed.getErrors()) {
            saveError(uploadBatch.getId(), error);
        }

        if (!parsed.getRows().isEmpty()) {
            StgBatch stgBatch = stagingBatchService.createBatch(
                    UPLOAD_SOURCE_ID, null, orgId, toStagingRecords(uploadBatch.getTemplateType(), parsed.getRows()));
            uploadBatch.setStgBatchId(stgBatch.getId());
            intUploadBatchMapper.updateById(uploadBatch);
        }

        if (parsed.getRows().isEmpty()) {
            throw new BusinessException(ErrorCode.INTEGRATION_TEMPLATE_MISMATCH, "模板无有效数据行");
        }
        return toVO(uploadBatch);
    }

    private String resolveStatus(ParsedUpload parsed) {
        if (parsed.getRows().isEmpty()) {
            return STATUS_FAILED;
        }
        if (!parsed.getErrors().isEmpty()) {
            return STATUS_PARTIAL_FAILED;
        }
        return STATUS_COMPLETED;
    }

    private List<StagingRecordDTO> toStagingRecords(String templateType, List<ParsedRow> rows) {
        List<StagingRecordDTO> records = new ArrayList<StagingRecordDTO>();
        for (ParsedRow row : rows) {
            StagingRecordDTO dto = new StagingRecordDTO();
            dto.setDomain(templateType);
            dto.setSourceRef(row.getSourceRef());
            dto.setRawPayload(row.getRawPayload());
            dto.setParseStatus("OK");
            records.add(dto);
        }
        return records;
    }

    private void saveError(Long uploadBatchId, ParsedError error) {
        IntUploadError entity = new IntUploadError();
        entity.setId(IdGenerator.nextId());
        entity.setUploadBatchId(uploadBatchId);
        entity.setRowNum(error.getRowNum());
        entity.setErrorMessage(error.getMessage());
        entity.setRowDataJson(error.getRowDataJson());
        intUploadErrorMapper.insert(entity);
    }

    private UploadResultVO toVO(IntUploadBatch batch) {
        UploadResultVO vo = new UploadResultVO();
        vo.setUploadBatchId(batch.getId());
        vo.setStgBatchId(batch.getStgBatchId());
        vo.setTemplateType(batch.getTemplateType());
        vo.setFileName(batch.getFileName());
        vo.setClientRequestId(batch.getClientRequestId());
        vo.setTotalRows(batch.getTotalRows());
        vo.setSuccessRows(batch.getSuccessRows());
        vo.setFailRows(batch.getFailRows());
        vo.setStatus(batch.getStatus());
        vo.setCreatedAt(batch.getCreatedAt());
        vo.setErrors(loadErrors(batch.getId()));
        return vo;
    }

    private List<UploadErrorVO> loadErrors(Long uploadBatchId) {
        List<IntUploadError> errors = intUploadErrorMapper.selectList(new LambdaQueryWrapper<IntUploadError>()
                .eq(IntUploadError::getUploadBatchId, uploadBatchId)
                .orderByAsc(IntUploadError::getRowNum));
        List<UploadErrorVO> result = new ArrayList<UploadErrorVO>();
        for (IntUploadError error : errors) {
            UploadErrorVO vo = new UploadErrorVO();
            vo.setRow(error.getRowNum());
            vo.setMessage(error.getErrorMessage());
            vo.setRowDataJson(error.getRowDataJson());
            result.add(vo);
        }
        return result;
    }
}

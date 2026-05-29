package com.nanda.ingestion.staging;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.event.StagingBatchReceivedEvent;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.ingestion.adapter.StagingRecordDTO;
import com.nanda.ingestion.domain.dto.StagingBatchVO;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.domain.entity.StgRecord;
import com.nanda.ingestion.domain.enums.BatchStatus;
import com.nanda.ingestion.mapper.StgBatchMapper;
import com.nanda.ingestion.mapper.StgRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StagingBatchService {

    private static final int MAX_RETRY = 3;

    private final StgBatchMapper stgBatchMapper;
    private final StgRecordMapper stgRecordMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PageResult<StagingBatchVO> list(PageQuery query, String status, String dateFrom) {
        Long orgId = AuthContextHolder.get().getOrgId();
        LambdaQueryWrapper<StgBatch> wrapper = new LambdaQueryWrapper<StgBatch>()
                .eq(StgBatch::getOrgId, orgId)
                .eq(StringUtils.hasText(status), StgBatch::getStatus, status)
                .orderByDesc(StgBatch::getReceivedAt);
        if (StringUtils.hasText(dateFrom)) {
            wrapper.ge(StgBatch::getReceivedAt, LocalDateTime.parse(dateFrom + "T00:00:00"));
        }
        Page<StgBatch> page = stgBatchMapper.selectPage(
                new Page<StgBatch>(query.getPage(), query.getSize()), wrapper);
        List<StagingBatchVO> items = new ArrayList<StagingBatchVO>();
        for (StgBatch batch : page.getRecords()) {
            items.add(toVO(batch));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public StagingBatchVO getById(Long id) {
        return toVO(requireBatch(id));
    }

    @Transactional
    public StgBatch createBatch(Long sourceId, Long jobId, Long orgId, List<StagingRecordDTO> records) {
        StgBatch batch = new StgBatch();
        batch.setId(IdGenerator.nextId());
        batch.setSourceId(sourceId);
        batch.setJobId(jobId);
        batch.setOrgId(orgId);
        batch.setReceivedAt(LocalDateTime.now());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setStatus(BatchStatus.RECEIVED);

        int success = 0;
        int fail = 0;
        for (StagingRecordDTO dto : records) {
            StgRecord record = new StgRecord();
            record.setId(IdGenerator.nextId());
            record.setBatchId(batch.getId());
            record.setDomain(dto.getDomain() != null ? dto.getDomain() : "OTHER");
            record.setRawPayload(dto.getRawPayload());
            record.setSourceRef(dto.getSourceRef());
            record.setOrgId(orgId);
            if ("OK".equals(dto.getParseStatus())) {
                record.setParseStatus("OK");
                success++;
            } else {
                record.setParseStatus("PARSE_ERROR");
                record.setParseError(dto.getParseError());
                fail++;
            }
            stgRecordMapper.insert(record);
        }
        batch.setRecordCount(records.size());
        batch.setSuccessCount(success);
        batch.setFailCount(fail);
        stgBatchMapper.insert(batch);

        if (success > 0) {
            eventPublisher.publishEvent(new StagingBatchReceivedEvent(this, batch.getId(), orgId, success));
        }
        return batch;
    }

    @Transactional
    public StagingBatchVO retry(Long batchId) {
        StgBatch batch = requireBatch(batchId);
        int retryCount = parseRetryCount(batch.getErrorMessage());
        if (retryCount >= MAX_RETRY) {
            throw new BusinessException(ErrorCode.INGESTION_BATCH_NOT_RETRYABLE, "批次重试次数已达上限");
        }
        if (!BatchStatus.REJECTED.equals(batch.getStatus())
                && !BatchStatus.RECEIVED.equals(batch.getStatus())) {
            throw new BusinessException(ErrorCode.INGESTION_BATCH_NOT_RETRYABLE, "当前批次状态不可重试");
        }
        List<StgRecord> failed = stgRecordMapper.selectList(new LambdaQueryWrapper<StgRecord>()
                .eq(StgRecord::getBatchId, batchId)
                .eq(StgRecord::getParseStatus, "PARSE_ERROR"));
        int recovered = 0;
        for (StgRecord record : failed) {
            if (record.getRawPayload() != null && !record.getRawPayload().isEmpty()) {
                record.setParseStatus("OK");
                record.setParseError(null);
                stgRecordMapper.updateById(record);
                recovered++;
            }
        }
        batch.setSuccessCount(batch.getSuccessCount() + recovered);
        batch.setFailCount(Math.max(0, batch.getFailCount() - recovered));
        batch.setStatus(BatchStatus.RECEIVED);
        batch.setErrorMessage("RETRY:" + (retryCount + 1));
        stgBatchMapper.updateById(batch);
        if (batch.getSuccessCount() > 0) {
            eventPublisher.publishEvent(new StagingBatchReceivedEvent(
                    this, batch.getId(), batch.getOrgId(), batch.getSuccessCount()));
        }
        return toVO(batch);
    }

    private int parseRetryCount(String errorMessage) {
        if (errorMessage != null && errorMessage.startsWith("RETRY:")) {
            try {
                return Integer.parseInt(errorMessage.substring(6));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private StgBatch requireBatch(Long id) {
        StgBatch batch = stgBatchMapper.selectById(id);
        if (batch == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "批次不存在");
        }
        Long orgId = AuthContextHolder.get().getOrgId();
        if (!batch.getOrgId().equals(orgId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该批次");
        }
        return batch;
    }

    private StagingBatchVO toVO(StgBatch batch) {
        StagingBatchVO vo = new StagingBatchVO();
        vo.setId(batch.getId());
        vo.setSourceId(batch.getSourceId());
        vo.setJobId(batch.getJobId());
        vo.setOrgId(batch.getOrgId());
        vo.setReceivedAt(batch.getReceivedAt());
        vo.setRecordCount(batch.getRecordCount());
        vo.setSuccessCount(batch.getSuccessCount());
        vo.setFailCount(batch.getFailCount());
        vo.setStatus(batch.getStatus());
        vo.setErrorMessage(batch.getErrorMessage());
        vo.setCreatedAt(batch.getCreatedAt());
        return vo;
    }
}

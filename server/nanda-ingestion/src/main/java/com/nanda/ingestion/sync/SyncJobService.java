package com.nanda.ingestion.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.ingestion.adapter.AdapterRegistry;
import com.nanda.ingestion.adapter.DataSourceConfig;
import com.nanda.ingestion.adapter.StagingRecordDTO;
import com.nanda.ingestion.adapter.SyncCursor;
import com.nanda.ingestion.domain.dto.StagingBatchVO;
import com.nanda.ingestion.domain.dto.SyncJobCreateRequest;
import com.nanda.ingestion.domain.dto.SyncJobVO;
import com.nanda.ingestion.domain.entity.StgBatch;
import com.nanda.ingestion.domain.entity.StgDatasource;
import com.nanda.ingestion.domain.entity.StgSyncJob;
import com.nanda.ingestion.domain.entity.StgSyncLog;
import com.nanda.ingestion.mapper.StgDatasourceMapper;
import com.nanda.ingestion.mapper.StgSyncJobMapper;
import com.nanda.ingestion.mapper.StgSyncLogMapper;
import com.nanda.ingestion.staging.StagingBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobService {

    private static final Set<String> VALID_SCHEDULE = new HashSet<String>(
            Arrays.asList("T7", "T1", "NEAR_RT", "MANUAL"));

    private final StgSyncJobMapper stgSyncJobMapper;
    private final StgSyncLogMapper stgSyncLogMapper;
    private final StgDatasourceMapper stgDatasourceMapper;
    private final AdapterRegistry adapterRegistry;
    private final StagingBatchService stagingBatchService;

    public List<SyncJobVO> list() {
        Long orgId = requireOrgId();
        List<StgSyncJob> jobs = stgSyncJobMapper.selectList(new LambdaQueryWrapper<StgSyncJob>()
                .eq(StgSyncJob::getOrgId, orgId)
                .eq(StgSyncJob::getDeleted, 0)
                .orderByDesc(StgSyncJob::getCreatedAt));
        List<SyncJobVO> result = new java.util.ArrayList<SyncJobVO>();
        for (StgSyncJob job : jobs) {
            result.add(toVO(job));
        }
        return result;
    }

    @Transactional
    public SyncJobVO create(SyncJobCreateRequest request) {
        if (!VALID_SCHEDULE.contains(request.getScheduleType())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "无效的调度类型");
        }
        StgDatasource ds = requireDatasource(request.getSourceId());
        AuthContext ctx = AuthContextHolder.get();
        StgSyncJob job = new StgSyncJob();
        job.setId(IdGenerator.nextId());
        job.setSourceId(request.getSourceId());
        job.setScheduleType(request.getScheduleType());
        job.setCronExpr(defaultCron(request.getScheduleType(), request.getCronExpr()));
        job.setOrgId(ds.getOrgId());
        job.setCreatedBy(ctx != null ? ctx.getUserId() : null);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        stgSyncJobMapper.insert(job);
        return toVO(job);
    }

    @Transactional
    public StagingBatchVO start(Long jobId) {
        StgSyncJob job = requireJob(jobId);
        StgDatasource ds = requireDatasource(job.getSourceId());

        StgSyncLog syncLog = new StgSyncLog();
        syncLog.setId(IdGenerator.nextId());
        syncLog.setJobId(jobId);
        syncLog.setStartedAt(LocalDateTime.now());
        syncLog.setOrgId(job.getOrgId());
        stgSyncLogMapper.insert(syncLog);

        try {
            DataSourceConfig config = adapterRegistry.parseConfig(ds.getConfigJson());
            List<StagingRecordDTO> records = adapterRegistry.getAdapter(ds.getProtocol())
                    .fetch(config, new SyncCursor());
            StgBatch batch = stagingBatchService.createBatch(ds.getId(), jobId, job.getOrgId(), records);

            job.setLastRunAt(LocalDateTime.now());
            job.setLastStatus("SUCCESS");
            job.setUpdatedAt(LocalDateTime.now());
            stgSyncJobMapper.updateById(job);

            syncLog.setFinishedAt(LocalDateTime.now());
            syncLog.setStatus("SUCCESS");
            syncLog.setMessage("batchId=" + batch.getId() + ", records=" + records.size());
            stgSyncLogMapper.updateById(syncLog);

            return stagingBatchService.getById(batch.getId());
        } catch (Exception e) {
            log.error("Sync job failed jobId={}", jobId, e);
            job.setLastRunAt(LocalDateTime.now());
            job.setLastStatus("FAILED");
            job.setUpdatedAt(LocalDateTime.now());
            stgSyncJobMapper.updateById(job);

            syncLog.setFinishedAt(LocalDateTime.now());
            syncLog.setStatus("FAILED");
            syncLog.setMessage(e.getMessage());
            stgSyncLogMapper.updateById(syncLog);

            throw new BusinessException(ErrorCode.INGESTION_CONNECTION_FAILED, "同步执行失败: " + e.getMessage());
        }
    }

    private String defaultCron(String scheduleType, String cronExpr) {
        if (cronExpr != null && !cronExpr.isEmpty()) {
            return cronExpr;
        }
        if ("T7".equals(scheduleType)) {
            return "0 0 2 ? * SUN";
        }
        if ("T1".equals(scheduleType)) {
            return "0 0 3 * * ?";
        }
        return null;
    }

    private StgSyncJob requireJob(Long id) {
        StgSyncJob job = stgSyncJobMapper.selectById(id);
        if (job == null || job.getDeleted() != null && job.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "同步任务不存在");
        }
        if (!job.getOrgId().equals(requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该任务");
        }
        return job;
    }

    private StgDatasource requireDatasource(Long id) {
        StgDatasource ds = stgDatasourceMapper.selectById(id);
        if (ds == null || ds.getDeleted() != null && ds.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "数据源不存在");
        }
        if (!ds.getOrgId().equals(requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该数据源");
        }
        return ds;
    }

    private Long requireOrgId() {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return ctx.getOrgId();
    }

    private SyncJobVO toVO(StgSyncJob job) {
        SyncJobVO vo = new SyncJobVO();
        vo.setId(job.getId());
        vo.setSourceId(job.getSourceId());
        vo.setScheduleType(job.getScheduleType());
        vo.setCronExpr(job.getCronExpr());
        vo.setLastRunAt(job.getLastRunAt());
        vo.setLastStatus(job.getLastStatus());
        vo.setOrgId(job.getOrgId());
        vo.setCreatedAt(job.getCreatedAt());
        return vo;
    }
}

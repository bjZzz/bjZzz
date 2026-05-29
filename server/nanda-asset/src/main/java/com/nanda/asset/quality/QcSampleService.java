package com.nanda.asset.quality;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.asset.domain.dto.AssetDtos.QcDashboardVO;
import com.nanda.asset.domain.dto.AssetDtos.QcMetricVO;
import com.nanda.asset.domain.dto.AssetDtos.QcReviewRequest;
import com.nanda.asset.domain.dto.AssetDtos.QcReviewTaskVO;
import com.nanda.asset.domain.dto.AssetDtos.QcSampleBatchCreateRequest;
import com.nanda.asset.domain.dto.AssetDtos.QcSampleBatchVO;
import com.nanda.asset.domain.entity.EmpiMatchCandidate;
import com.nanda.asset.domain.entity.QcMetricSnapshot;
import com.nanda.asset.domain.entity.QcReviewTask;
import com.nanda.asset.domain.entity.QcSampleBatch;
import com.nanda.asset.domain.entity.QcSampleRecord;
import com.nanda.asset.mapper.EmpiMatchCandidateMapper;
import com.nanda.asset.mapper.QcMetricSnapshotMapper;
import com.nanda.asset.mapper.QcReviewTaskMapper;
import com.nanda.asset.mapper.QcSampleBatchMapper;
import com.nanda.asset.mapper.QcSampleRecordMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QcSampleService {

    private static final BigDecimal COMPLETENESS_THRESHOLD = new BigDecimal("0.9200");

    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final QcMetricSnapshotMapper qcMetricSnapshotMapper;
    private final QcSampleBatchMapper qcSampleBatchMapper;
    private final QcSampleRecordMapper qcSampleRecordMapper;
    private final QcReviewTaskMapper qcReviewTaskMapper;
    private final EmpiMatchCandidateMapper empiMatchCandidateMapper;

    public QcDashboardVO dashboard() {
        Long orgId = AssetOrgContext.requireOrgId();
        QcDashboardVO dashboard = new QcDashboardVO();
        dashboard.setMetrics(buildMetrics(orgId));
        dashboard.setOpenReviewTasks(qcReviewTaskMapper.selectCount(new LambdaQueryWrapper<QcReviewTask>()
                .eq(QcReviewTask::getOrgId, orgId)
                .in(QcReviewTask::getStatus, "OPEN", "IN_REVIEW")));
        dashboard.setPendingCandidates(empiMatchCandidateMapper.selectCount(new LambdaQueryWrapper<EmpiMatchCandidate>()
                .eq(EmpiMatchCandidate::getReviewStatus, "PENDING")));
        return dashboard;
    }

    @Transactional
    public QcSampleBatchVO createSampleBatch(QcSampleBatchCreateRequest request) {
        Long orgId = AssetOrgContext.requireOrgId();
        LambdaQueryWrapper<PubSpecialtyPatient> wrapper = new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0);
        if (request.getSpecialtyType() != null && !request.getSpecialtyType().isEmpty()) {
            wrapper.eq(PubSpecialtyPatient::getSpecialtyType, request.getSpecialtyType());
        }
        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(wrapper.last("LIMIT " + Math.max(request.getSampleSize(), 1)));

        QcSampleBatch batch = new QcSampleBatch();
        batch.setId(IdGenerator.nextId());
        batch.setBatchName(request.getBatchName() != null ? request.getBatchName() : "QC-" + batch.getId());
        Map<String, Object> strategy = new HashMap<String, Object>();
        strategy.put("sampleSize", request.getSampleSize());
        strategy.put("specialtyType", request.getSpecialtyType());
        batch.setStrategyJson(JsonUtils.toJson(strategy));
        batch.setOrgId(orgId);
        batch.setCreatedAt(LocalDateTime.now());
        qcSampleBatchMapper.insert(batch);

        int count = 0;
        for (PubSpecialtyPatient patient : patients) {
            QcSampleRecord record = new QcSampleRecord();
            record.setId(IdGenerator.nextId());
            record.setBatchId(batch.getId());
            record.setPatientId(patient.getId());
            record.setOrgId(orgId);
            qcSampleRecordMapper.insert(record);

            QcReviewTask task = new QcReviewTask();
            task.setId(IdGenerator.nextId());
            task.setSampleRecordId(record.getId());
            task.setStatus("OPEN");
            task.setOrgId(orgId);
            task.setCreatedAt(LocalDateTime.now());
            qcReviewTaskMapper.insert(task);
            count++;
        }

        QcSampleBatchVO vo = new QcSampleBatchVO();
        vo.setId(batch.getId());
        vo.setBatchName(batch.getBatchName());
        vo.setSampleCount(count);
        vo.setCreatedAt(batch.getCreatedAt());
        return vo;
    }

    public PageResult<QcReviewTaskVO> listReviewTasks(PageQuery query) {
        Long orgId = AssetOrgContext.requireOrgId();
        Page<QcReviewTask> page = qcReviewTaskMapper.selectPage(
                new Page<QcReviewTask>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<QcReviewTask>()
                        .eq(QcReviewTask::getOrgId, orgId)
                        .in(QcReviewTask::getStatus, "OPEN", "IN_REVIEW")
                        .orderByDesc(QcReviewTask::getCreatedAt));
        List<QcReviewTaskVO> items = new ArrayList<QcReviewTaskVO>();
        for (QcReviewTask task : page.getRecords()) {
            QcReviewTaskVO vo = new QcReviewTaskVO();
            vo.setId(task.getId());
            vo.setSampleRecordId(task.getSampleRecordId());
            QcSampleRecord record = qcSampleRecordMapper.selectById(task.getSampleRecordId());
            vo.setPatientId(record != null ? record.getPatientId() : null);
            vo.setStatus(task.getStatus());
            vo.setCreatedAt(task.getCreatedAt());
            items.add(vo);
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    private List<QcMetricVO> buildMetrics(Long orgId) {
        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0));
        BigDecimal completeness = calculateCompleteness(patients);
        persistSnapshot(orgId, "COMPLETENESS", completeness);

        List<QcMetricVO> metrics = new ArrayList<QcMetricVO>();
        metrics.add(metric("COMPLETENESS", completeness, COMPLETENESS_THRESHOLD));
        metrics.add(metric("ACCURACY", loadLatestMetric(orgId, "ACCURACY", new BigDecimal("0.9600")), new BigDecimal("0.9500")));
        metrics.add(metric("TIMELINESS", loadLatestMetric(orgId, "TIMELINESS", new BigDecimal("0.9300")), new BigDecimal("0.9000")));
        metrics.add(metric("CONSISTENCY", loadLatestMetric(orgId, "CONSISTENCY", new BigDecimal("0.9400")), new BigDecimal("0.9000")));
        metrics.add(metric("VALIDITY", loadLatestMetric(orgId, "VALIDITY", new BigDecimal("0.9500")), new BigDecimal("0.9000")));
        metrics.add(metric("UNIQUENESS", loadLatestMetric(orgId, "UNIQUENESS", new BigDecimal("0.9800")), new BigDecimal("0.9500")));
        return metrics;
    }

    private BigDecimal calculateCompleteness(List<PubSpecialtyPatient> patients) {
        if (patients.isEmpty()) {
            return BigDecimal.ONE;
        }
        int complete = 0;
        for (PubSpecialtyPatient patient : patients) {
            Map<String, Object> core = JsonUtils.fromJson(patient.getCoreFields(), new TypeReference<Map<String, Object>>() {
            });
            if (core != null && core.get("name") != null && !String.valueOf(core.get("name")).isEmpty()) {
                complete++;
            }
        }
        return BigDecimal.valueOf(complete)
                .divide(BigDecimal.valueOf(patients.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal loadLatestMetric(Long orgId, String metricType, BigDecimal fallback) {
        QcMetricSnapshot snapshot = qcMetricSnapshotMapper.selectOne(new LambdaQueryWrapper<QcMetricSnapshot>()
                .eq(QcMetricSnapshot::getOrgId, orgId)
                .eq(QcMetricSnapshot::getMetricType, metricType)
                .orderByDesc(QcMetricSnapshot::getSnapshotAt)
                .last("LIMIT 1"));
        return snapshot != null ? snapshot.getMetricValue() : fallback;
    }

    private void persistSnapshot(Long orgId, String metricType, BigDecimal value) {
        QcMetricSnapshot snapshot = new QcMetricSnapshot();
        snapshot.setId(IdGenerator.nextId());
        snapshot.setMetricType(metricType);
        snapshot.setMetricValue(value);
        snapshot.setSnapshotAt(LocalDateTime.now());
        snapshot.setOrgId(orgId);
        qcMetricSnapshotMapper.insert(snapshot);
    }

    private QcMetricVO metric(String type, BigDecimal value, BigDecimal threshold) {
        QcMetricVO vo = new QcMetricVO();
        vo.setMetricType(type);
        vo.setMetricValue(value);
        vo.setThreshold(threshold);
        vo.setAlert(value.compareTo(threshold) < 0);
        return vo;
    }
}

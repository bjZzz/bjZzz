package com.nanda.analytics.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardDataVO;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.DashboardVO;
import com.nanda.analytics.domain.entity.AnaDashboard;
import com.nanda.analytics.domain.entity.AnaExportTask;
import com.nanda.analytics.domain.entity.AnaRiskAssessment;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.analytics.mapper.AnaDashboardMapper;
import com.nanda.analytics.mapper.AnaExportTaskMapper;
import com.nanda.analytics.mapper.AnaRiskAssessmentMapper;
import com.nanda.analytics.mapper.IdxSearchDocumentMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnaDashboardMapper anaDashboardMapper;
    private final IdxSearchDocumentMapper idxSearchDocumentMapper;
    private final AnaExportTaskMapper anaExportTaskMapper;
    private final AnaRiskAssessmentMapper anaRiskAssessmentMapper;

    public List<DashboardVO> list() {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        List<AnaDashboard> dashboards = anaDashboardMapper.selectList(new LambdaQueryWrapper<AnaDashboard>()
                .eq(AnaDashboard::getOrgId, orgId)
                .orderByDesc(AnaDashboard::getCreatedAt));
        List<DashboardVO> result = new ArrayList<DashboardVO>();
        for (AnaDashboard dashboard : dashboards) {
            result.add(toVO(dashboard));
        }
        return result;
    }

    @Transactional
    public DashboardVO create(DashboardCreateRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        AnaDashboard dashboard = new AnaDashboard();
        dashboard.setId(IdGenerator.nextId());
        dashboard.setDashboardName(request.getDashboardName());
        dashboard.setConfigJson(request.getConfigJson());
        dashboard.setUserId(userId);
        dashboard.setOrgId(orgId);
        dashboard.setCreatedAt(LocalDateTime.now());
        anaDashboardMapper.insert(dashboard);
        return toVO(dashboard);
    }

    @Transactional
    public DashboardVO updateLayout(Long id, DashboardCreateRequest request) {
        AnaDashboard dashboard = requireDashboard(id);
        if (request.getDashboardName() != null) {
            dashboard.setDashboardName(request.getDashboardName());
        }
        if (request.getConfigJson() != null) {
            dashboard.setConfigJson(request.getConfigJson());
        }
        anaDashboardMapper.updateById(dashboard);
        return toVO(dashboard);
    }

    public DashboardDataVO data() {
        Long orgId = AnalyticsOrgContext.requireOrgId();

        long indexedPatients = idxSearchDocumentMapper.selectCount(new LambdaQueryWrapper<IdxSearchDocument>()
                .eq(IdxSearchDocument::getOrgId, orgId));
        long exportPending = anaExportTaskMapper.selectCount(new LambdaQueryWrapper<AnaExportTask>()
                .eq(AnaExportTask::getOrgId, orgId)
                .eq(AnaExportTask::getStatus, "PENDING_APPROVAL"));
        long exportCompleted = anaExportTaskMapper.selectCount(new LambdaQueryWrapper<AnaExportTask>()
                .eq(AnaExportTask::getOrgId, orgId)
                .eq(AnaExportTask::getStatus, "COMPLETED"));
        long riskAssessments = anaRiskAssessmentMapper.selectCount(new LambdaQueryWrapper<AnaRiskAssessment>()
                .eq(AnaRiskAssessment::getOrgId, orgId));

        Map<String, Object> metrics = new LinkedHashMap<String, Object>();
        metrics.put("indexedPatients", indexedPatients);
        metrics.put("exportTasksPendingApproval", exportPending);
        metrics.put("exportTasksCompleted", exportCompleted);
        metrics.put("riskAssessments", riskAssessments);

        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        series.add(riskLevelSeries(orgId));

        DashboardDataVO vo = new DashboardDataVO();
        vo.setMetrics(metrics);
        vo.setSeries(series);
        return vo;
    }

    private Map<String, Object> riskLevelSeries(Long orgId) {
        List<AnaRiskAssessment> records = anaRiskAssessmentMapper.selectList(new LambdaQueryWrapper<AnaRiskAssessment>()
                .eq(AnaRiskAssessment::getOrgId, orgId));
        Map<String, Object> counts = new LinkedHashMap<String, Object>();
        for (AnaRiskAssessment record : records) {
            String level = record.getRiskLevel() != null ? record.getRiskLevel() : "UNKNOWN";
            Object current = counts.get(level);
            counts.put(level, current == null ? 1 : ((Integer) current) + 1);
        }
        Map<String, Object> series = new LinkedHashMap<String, Object>();
        series.put("name", "riskLevelDistribution");
        series.put("data", counts);
        return series;
    }

    private AnaDashboard requireDashboard(Long id) {
        AnaDashboard dashboard = anaDashboardMapper.selectById(id);
        if (dashboard == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "仪表盘不存在");
        }
        if (!dashboard.getOrgId().equals(AnalyticsOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该仪表盘");
        }
        return dashboard;
    }

    private DashboardVO toVO(AnaDashboard dashboard) {
        DashboardVO vo = new DashboardVO();
        vo.setId(dashboard.getId());
        vo.setDashboardName(dashboard.getDashboardName());
        vo.setConfigJson(dashboard.getConfigJson());
        vo.setCreatedAt(dashboard.getCreatedAt());
        return vo;
    }
}

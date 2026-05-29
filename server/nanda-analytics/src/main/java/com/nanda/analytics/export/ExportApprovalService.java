package com.nanda.analytics.export;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportApproveRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportDownloadVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportRejectRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ExportTaskVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ImportToProjectRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ImportToProjectResultVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import com.nanda.analytics.domain.entity.AnaExportFile;
import com.nanda.analytics.domain.entity.AnaExportTask;
import com.nanda.analytics.domain.entity.AnaSearchQuery;
import com.nanda.analytics.mapper.AnaExportFileMapper;
import com.nanda.analytics.mapper.AnaExportTaskMapper;
import com.nanda.analytics.mapper.AnaSearchQueryMapper;
import com.nanda.analytics.search.SearchService;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.cohort.CohortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportApprovalService {

    private final AnaExportTaskMapper anaExportTaskMapper;
    private final AnaExportFileMapper anaExportFileMapper;
    private final AnaSearchQueryMapper anaSearchQueryMapper;
    private final SearchService searchService;
    private final CdiscExportAdapter cdiscExportAdapter;
    private final ExportFileBuilder exportFileBuilder;
    private final CohortService cohortService;

    public PageResult<ExportTaskVO> list(PageQuery query) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Page<AnaExportTask> page = anaExportTaskMapper.selectPage(
                new Page<AnaExportTask>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<AnaExportTask>()
                        .eq(AnaExportTask::getOrgId, orgId)
                        .orderByDesc(AnaExportTask::getCreatedAt));
        List<ExportTaskVO> items = new ArrayList<ExportTaskVO>();
        for (AnaExportTask task : page.getRecords()) {
            items.add(toVO(task));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public ExportTaskVO get(Long id) {
        return toVO(requireTask(id));
    }

    @Transactional
    public ExportTaskVO create(ExportCreateRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        Long searchQueryId = request.getSearchQueryId();
        String queryJson = request.getQueryJson();

        if (searchQueryId != null) {
            AnaSearchQuery saved = anaSearchQueryMapper.selectById(searchQueryId);
            if (saved == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "检索条件不存在");
            }
            queryJson = saved.getQueryJson();
        }
        if (queryJson == null || queryJson.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少检索条件");
        }
        searchService.parseQuery(queryJson);

        AnaExportTask task = new AnaExportTask();
        task.setId(IdGenerator.nextId());
        task.setSearchQueryId(searchQueryId);
        task.setExportFormat(request.getExportFormat() != null ? request.getExportFormat() : "CSV");
        task.setExportScopeJson(queryJson);
        task.setStatus("DRAFT");
        task.setUserId(userId);
        task.setOrgId(orgId);
        task.setCreatedAt(LocalDateTime.now());
        anaExportTaskMapper.insert(task);
        return toVO(task);
    }

    @Transactional
    public ExportTaskVO submit(Long id) {
        AnaExportTask task = requireTask(id);
        if (!"DRAFT".equals(task.getStatus()) && !"REJECTED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不可提交审核");
        }
        task.setStatus("PENDING_APPROVAL");
        anaExportTaskMapper.updateById(task);
        return toVO(task);
    }

    @Transactional
    public ExportTaskVO approve(Long id, ExportApproveRequest request) {
        AnaExportTask task = requireTask(id);
        if (!"PENDING_APPROVAL".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不可审批");
        }
        task.setStatus("APPROVED");
        task.setApproverId(AnalyticsOrgContext.currentUserId());
        task.setApprovedAt(LocalDateTime.now());
        anaExportTaskMapper.updateById(task);

        task.setStatus("PROCESSING");
        anaExportTaskMapper.updateById(task);
        generateExportFile(task);
        task.setStatus("COMPLETED");
        anaExportTaskMapper.updateById(task);
        return toVO(task);
    }

    @Transactional
    public ExportTaskVO reject(Long id, ExportRejectRequest request) {
        AnaExportTask task = requireTask(id);
        if (!"PENDING_APPROVAL".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前状态不可驳回");
        }
        task.setStatus("REJECTED");
        task.setApproverId(AnalyticsOrgContext.currentUserId());
        task.setApprovedAt(LocalDateTime.now());
        anaExportTaskMapper.updateById(task);
        return toVO(task);
    }

    public ExportDownloadVO download(Long id) {
        AnaExportTask task = requireTask(id);
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "导出尚未完成");
        }
        AnaExportFile file = anaExportFileMapper.selectOne(new LambdaQueryWrapper<AnaExportFile>()
                .eq(AnaExportFile::getTaskId, id)
                .orderByDesc(AnaExportFile::getCreatedAt)
                .last("LIMIT 1"));
        if (file == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出文件不存在");
        }
        ExportDownloadVO vo = new ExportDownloadVO();
        vo.setFileName(exportFileBuilder.resolveFileName(id, task.getExportFormat()));
        vo.setContentType(exportFileBuilder.resolveContentType(task.getExportFormat()));
        vo.setContent(exportFileBuilder.readFile(file.getFileRef()));
        return vo;
    }

    @Transactional
    public ImportToProjectResultVO importToProject(ImportToProjectRequest request) {
        AnaExportTask task = requireTask(request.getExportTaskId());
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "导出任务未完成，无法入组");
        }
        List<Long> empiIds = searchService.resolveEmpiIds(task.getExportScopeJson());
        int enrolled = cohortService.batchEnroll(request.getCohortId(), empiIds, request.getGroupLabel());
        ImportToProjectResultVO result = new ImportToProjectResultVO();
        result.setEnrolled(enrolled);
        return result;
    }

    private void generateExportFile(AnaExportTask task) {
        List<SearchHitVO> hits = searchService.resolveExecutorHits(task.getExportScopeJson());
        byte[] content = exportFileBuilder.buildContent(task.getExportFormat(), hits, task.getId(), cdiscExportAdapter);
        String fileRef = exportFileBuilder.writeFile(task.getId(), task.getExportFormat(), content);

        AnaExportFile file = new AnaExportFile();
        file.setId(IdGenerator.nextId());
        file.setTaskId(task.getId());
        file.setFileRef(fileRef);
        file.setFileSize((long) content.length);
        file.setCreatedAt(LocalDateTime.now());
        anaExportFileMapper.insert(file);
    }

    private AnaExportTask requireTask(Long id) {
        AnaExportTask task = anaExportTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "导出任务不存在");
        }
        if (!task.getOrgId().equals(AnalyticsOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该导出任务");
        }
        return task;
    }

    private ExportTaskVO toVO(AnaExportTask task) {
        ExportTaskVO vo = new ExportTaskVO();
        vo.setId(task.getId());
        vo.setSearchQueryId(task.getSearchQueryId());
        vo.setExportFormat(task.getExportFormat());
        vo.setStatus(task.getStatus());
        vo.setApproverId(task.getApproverId());
        vo.setApprovedAt(task.getApprovedAt());
        vo.setCreatedAt(task.getCreatedAt());
        return vo;
    }
}

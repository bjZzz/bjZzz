package com.nanda.analytics.sandbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.NotebookSaveRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.NotebookVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobSubmitRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxSessionVO;
import com.nanda.analytics.domain.entity.AnaAnalyticsJob;
import com.nanda.analytics.domain.entity.AnaAnalyticsResult;
import com.nanda.analytics.domain.entity.AnaSandboxSession;
import com.nanda.analytics.mapper.AnaAnalyticsJobMapper;
import com.nanda.analytics.mapper.AnaAnalyticsResultMapper;
import com.nanda.analytics.mapper.AnaSandboxSessionMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SandboxService {

    private final AnaSandboxSessionMapper sandboxSessionMapper;
    private final AnaAnalyticsJobMapper analyticsJobMapper;
    private final AnaAnalyticsResultMapper analyticsResultMapper;
    private final SandboxClient sandboxClient;

    @Transactional
    public SandboxSessionVO createSession() {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        AnaSandboxSession active = sandboxSessionMapper.selectOne(new LambdaQueryWrapper<AnaSandboxSession>()
                .eq(AnaSandboxSession::getUserId, userId)
                .eq(AnaSandboxSession::getOrgId, orgId)
                .eq(AnaSandboxSession::getStatus, "ACTIVE")
                .orderByDesc(AnaSandboxSession::getLastActiveAt)
                .last("LIMIT 1"));
        if (active != null) {
            return toSessionVO(active);
        }

        String workspaceId = "ws-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        SandboxClient.WorkspaceResponse workspace = sandboxClient.createWorkspace(userId, orgId, workspaceId);

        AnaSandboxSession session = new AnaSandboxSession();
        session.setId(IdGenerator.nextId());
        session.setUserId(userId);
        session.setOrgId(orgId);
        session.setWorkspaceId(workspace.getWorkspaceId());
        session.setStatus(workspace.getStatus());
        session.setKernelStatus(workspace.getKernelStatus());
        session.setLastActiveAt(LocalDateTime.now());
        sandboxSessionMapper.insert(session);
        return toSessionVO(session);
    }

    public SandboxSessionVO getSession(Long sessionId) {
        return toSessionVO(requireSession(sessionId));
    }

    public NotebookVO getNotebook(Long sessionId, String notebookId) {
        AnaSandboxSession session = requireSession(sessionId);
        String content = sandboxClient.getNotebook(session.getWorkspaceId(), notebookId,
                session.getUserId(), session.getOrgId());
        NotebookVO vo = new NotebookVO();
        vo.setNotebookId(notebookId);
        vo.setContent(content);
        vo.setUpdatedAt(LocalDateTime.now());
        return vo;
    }

    @Transactional
    public NotebookVO saveNotebook(Long sessionId, String notebookId, NotebookSaveRequest request) {
        AnaSandboxSession session = requireSession(sessionId);
        sandboxClient.saveNotebook(session.getWorkspaceId(), notebookId, request.getContent(),
                session.getUserId(), session.getOrgId());
        session.setLastActiveAt(LocalDateTime.now());
        sandboxSessionMapper.updateById(session);
        NotebookVO vo = new NotebookVO();
        vo.setNotebookId(notebookId);
        vo.setContent(request.getContent());
        vo.setUpdatedAt(session.getLastActiveAt());
        return vo;
    }

    @Transactional
    public SandboxJobVO submitJob(SandboxJobSubmitRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("jobType", request.getJobType());
        payload.put("methodCode", request.getMethodCode());
        payload.put("input", request.getInput());
        payload.put("datasetId", request.getDatasetId());
        payload.put("scriptContent", request.getScriptContent());

        SandboxClient.JobResponse remote = sandboxClient.submitJob(payload, userId, orgId);

        AnaAnalyticsJob job = new AnaAnalyticsJob();
        job.setId(IdGenerator.nextId());
        job.setJobType(request.getJobType() != null ? request.getJobType() : "SANDBOX");
        job.setMethodCode(request.getMethodCode());
        job.setInputJson(JsonUtils.toJson(request.getInput()));
        job.setStatus(remote.getStatus());
        job.setSandboxJobId(remote.getSandboxJobId());
        job.setUserId(userId);
        job.setOrgId(orgId);
        job.setCreatedAt(LocalDateTime.now());
        analyticsJobMapper.insert(job);

        if (remote.getResult() != null && !remote.getResult().isEmpty()) {
            persistResult(job.getId(), remote.getResult());
            job.setStatus("SUCCEEDED");
            analyticsJobMapper.updateById(job);
        }

        return toJobVO(job, remote.getResult());
    }

    @Transactional
    public SandboxJobVO submitStatJob(String methodCode, Map<String, Object> input) {
        SandboxJobSubmitRequest request = new SandboxJobSubmitRequest();
        request.setJobType("STATISTICS");
        request.setMethodCode(methodCode);
        request.setInput(input);
        return submitJob(request);
    }

    @Transactional
    public SandboxJobVO submitRiskJob(String modelCode, Map<String, Object> input) {
        SandboxJobSubmitRequest request = new SandboxJobSubmitRequest();
        request.setJobType("RISK");
        request.setMethodCode(modelCode);
        request.setInput(input);
        return submitJob(request);
    }

    public SandboxJobVO getJob(Long jobId) {
        AnaAnalyticsJob job = requireJob(jobId);
        Map<String, Object> result = loadResult(job.getId());
        if (job.getSandboxJobId() != null && (result == null || result.isEmpty())) {
            SandboxClient.JobResponse remote = sandboxClient.getJob(
                    job.getSandboxJobId(), job.getUserId(), job.getOrgId());
            if (remote.getResult() != null && !remote.getResult().isEmpty()) {
                result = remote.getResult();
            }
            if (remote.getStatus() != null && !remote.getStatus().equals(job.getStatus())) {
                job.setStatus(remote.getStatus());
                analyticsJobMapper.updateById(job);
            }
            if ("SUCCEEDED".equals(remote.getStatus()) && result != null) {
                persistResult(job.getId(), result);
            }
        }
        return toJobVO(job, result);
    }

    @Transactional
    public SandboxJobVO cancelJob(Long jobId) {
        AnaAnalyticsJob job = requireJob(jobId);
        job.setStatus("CANCELLED");
        analyticsJobMapper.updateById(job);
        return toJobVO(job, loadResult(job.getId()));
    }

    private AnaSandboxSession requireSession(Long sessionId) {
        AnaSandboxSession session = sandboxSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "沙箱会话不存在");
        }
        Long orgId = AnalyticsOrgContext.requireOrgId();
        if (!session.getOrgId().equals(orgId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该沙箱会话");
        }
        return session;
    }

    private AnaAnalyticsJob requireJob(Long jobId) {
        AnaAnalyticsJob job = analyticsJobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "沙箱作业不存在");
        }
        Long orgId = AnalyticsOrgContext.requireOrgId();
        if (!job.getOrgId().equals(orgId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该沙箱作业");
        }
        return job;
    }

    private void persistResult(Long jobId, Map<String, Object> resultData) {
        AnaAnalyticsResult existing = analyticsResultMapper.selectOne(
                new LambdaQueryWrapper<AnaAnalyticsResult>()
                        .eq(AnaAnalyticsResult::getJobId, jobId)
                        .last("LIMIT 1"));
        if (existing != null) {
            existing.setResultJson(JsonUtils.toJson(resultData));
            analyticsResultMapper.updateById(existing);
            return;
        }
        AnaAnalyticsResult result = new AnaAnalyticsResult();
        result.setId(IdGenerator.nextId());
        result.setJobId(jobId);
        result.setResultJson(JsonUtils.toJson(resultData));
        result.setCreatedAt(LocalDateTime.now());
        analyticsResultMapper.insert(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadResult(Long jobId) {
        AnaAnalyticsResult result = analyticsResultMapper.selectOne(
                new LambdaQueryWrapper<AnaAnalyticsResult>()
                        .eq(AnaAnalyticsResult::getJobId, jobId)
                        .last("LIMIT 1"));
        if (result == null || result.getResultJson() == null) {
            return null;
        }
        return JsonUtils.fromJson(result.getResultJson(), Map.class);
    }

    private SandboxSessionVO toSessionVO(AnaSandboxSession session) {
        SandboxSessionVO vo = new SandboxSessionVO();
        vo.setSessionId(session.getId());
        vo.setWorkspaceId(session.getWorkspaceId());
        vo.setStatus(session.getStatus());
        vo.setKernelStatus(session.getKernelStatus());
        vo.setLastActiveAt(session.getLastActiveAt());
        return vo;
    }

    private SandboxJobVO toJobVO(AnaAnalyticsJob job, Map<String, Object> result) {
        SandboxJobVO vo = new SandboxJobVO();
        vo.setJobId(job.getId());
        vo.setSandboxJobId(job.getSandboxJobId());
        vo.setStatus(job.getStatus());
        vo.setResult(result);
        vo.setCreatedAt(job.getCreatedAt());
        return vo;
    }
}

package com.nanda.analytics.statistics;

import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.StatRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.StatResultVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobVO;
import com.nanda.analytics.domain.entity.AnaAnalyticsJob;
import com.nanda.analytics.domain.entity.AnaAnalyticsResult;
import com.nanda.analytics.mapper.AnaAnalyticsJobMapper;
import com.nanda.analytics.mapper.AnaAnalyticsResultMapper;
import com.nanda.analytics.sandbox.SandboxService;
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

@Service
@RequiredArgsConstructor
public class StatService {

    private final StatMethodRegistry statMethodRegistry;
    private final AnaAnalyticsJobMapper anaAnalyticsJobMapper;
    private final AnaAnalyticsResultMapper anaAnalyticsResultMapper;
    private final SandboxService sandboxService;

    public Map<String, Object> listMethods() {
        return statMethodRegistry.listMethods();
    }

    @Transactional
    public StatResultVO execute(String method, StatRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        Long userId = AnalyticsOrgContext.currentUserId();
        Map<String, Object> input = request.getInput();

        Map<String, Object> resultData;
        String status;
        if (statMethodRegistry.isJavaMethod(method)) {
            resultData = statMethodRegistry.getJavaMethod(method).execute(input);
            status = "SUCCEEDED";
        } else if (statMethodRegistry.isSandboxMethod(method)) {
            SandboxJobVO sandboxJob = sandboxService.submitStatJob(method, input);
            resultData = new LinkedHashMap<String, Object>();
            resultData.put("delegatedTo", "sandbox");
            resultData.put("sandboxJobId", sandboxJob.getSandboxJobId());
            resultData.put("status", sandboxJob.getStatus());
            StatResultVO vo = new StatResultVO();
            vo.setJobId(sandboxJob.getJobId());
            vo.setMethod(method);
            vo.setResult(resultData);
            return vo;
        } else {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不支持的统计方法: " + method);
        }

        Long jobId = null;
        if (request.isPersist() || "QUEUED".equals(status)) {
            jobId = persistJob(method, input, status, userId, orgId);
            if ("SUCCEEDED".equals(status)) {
                persistResult(jobId, resultData);
            }
        }

        StatResultVO vo = new StatResultVO();
        vo.setJobId(jobId);
        vo.setMethod(method);
        vo.setResult(resultData);
        return vo;
    }

    private Long persistJob(String method, Map<String, Object> input, String status, Long userId, Long orgId) {
        AnaAnalyticsJob job = new AnaAnalyticsJob();
        job.setId(IdGenerator.nextId());
        job.setJobType("STATISTICS");
        job.setMethodCode(method);
        job.setInputJson(JsonUtils.toJson(input));
        job.setStatus(status);
        job.setUserId(userId);
        job.setOrgId(orgId);
        job.setCreatedAt(LocalDateTime.now());
        anaAnalyticsJobMapper.insert(job);
        return job.getId();
    }

    private void persistResult(Long jobId, Map<String, Object> resultData) {
        AnaAnalyticsResult result = new AnaAnalyticsResult();
        result.setId(IdGenerator.nextId());
        result.setJobId(jobId);
        result.setResultJson(JsonUtils.toJson(resultData));
        result.setCreatedAt(LocalDateTime.now());
        anaAnalyticsResultMapper.insert(result);
    }
}

package com.nanda.analytics.risk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.RiskAssessRequest;
import com.nanda.analytics.domain.dto.AnalyticsW6Dtos.RiskResultVO;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.SandboxJobVO;
import com.nanda.analytics.domain.entity.AnaRiskAssessment;
import com.nanda.analytics.mapper.AnaRiskAssessmentMapper;
import com.nanda.analytics.sandbox.SandboxService;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {

    private static final java.util.Set<String> SANDBOX_MODELS = new java.util.HashSet<String>(
            java.util.Arrays.asList("comorbidity-network", "causal-analysis"));

    private final RiskCalculatorFactory riskCalculatorFactory;
    private final AnaRiskAssessmentMapper anaRiskAssessmentMapper;
    private final SandboxService sandboxService;

    public List<String> supportedModels() {
        return riskCalculatorFactory.supportedModels();
    }

    @Transactional
    public RiskResultVO assess(String modelCode, RiskAssessRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        if (SANDBOX_MODELS.contains(modelCode)) {
            SandboxJobVO sandboxJob = sandboxService.submitRiskJob(modelCode, request.getInput());
            AnaRiskAssessment entity = new AnaRiskAssessment();
            entity.setId(IdGenerator.nextId());
            entity.setEmpiId(request.getEmpiId());
            entity.setModelCode(modelCode);
            entity.setInputJson(JsonUtils.toJson(request.getInput()));
            java.util.Map<String, Object> detail = new java.util.LinkedHashMap<String, Object>();
            detail.put("delegatedTo", "sandbox");
            detail.put("sandboxJobId", sandboxJob.getSandboxJobId());
            detail.put("jobId", sandboxJob.getJobId());
            detail.put("status", sandboxJob.getStatus());
            entity.setResultJson(JsonUtils.toJson(detail));
            entity.setRiskLevel("QUEUED");
            entity.setAssessedAt(LocalDateTime.now());
            entity.setOrgId(orgId);
            anaRiskAssessmentMapper.insert(entity);
            RiskResultVO vo = new RiskResultVO();
            vo.setId(entity.getId());
            vo.setModelCode(modelCode);
            vo.setRiskLevel(entity.getRiskLevel());
            vo.setAssessedAt(entity.getAssessedAt());
            vo.setDetail(detail);
            return vo;
        }
        RiskCalculator calculator = riskCalculatorFactory.get(modelCode);
        RiskResult result = calculator.calculate(request.getInput());

        AnaRiskAssessment entity = new AnaRiskAssessment();
        entity.setId(IdGenerator.nextId());
        entity.setEmpiId(request.getEmpiId());
        entity.setModelCode(modelCode);
        entity.setInputJson(JsonUtils.toJson(request.getInput()));
        entity.setResultJson(JsonUtils.toJson(result.getDetail()));
        entity.setRiskLevel(result.getRiskLevel());
        entity.setAssessedAt(LocalDateTime.now());
        entity.setOrgId(orgId);
        anaRiskAssessmentMapper.insert(entity);

        return toVO(entity, result);
    }

    public List<RiskResultVO> historyByEmpi(Long empiId) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        List<AnaRiskAssessment> records = anaRiskAssessmentMapper.selectList(
                new LambdaQueryWrapper<AnaRiskAssessment>()
                        .eq(AnaRiskAssessment::getEmpiId, empiId)
                        .eq(AnaRiskAssessment::getOrgId, orgId)
                        .orderByDesc(AnaRiskAssessment::getAssessedAt));
        List<RiskResultVO> result = new ArrayList<RiskResultVO>();
        for (AnaRiskAssessment record : records) {
            result.add(toVO(record, null));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private RiskResultVO toVO(AnaRiskAssessment entity, RiskResult result) {
        RiskResultVO vo = new RiskResultVO();
        vo.setId(entity.getId());
        vo.setModelCode(entity.getModelCode());
        vo.setRiskLevel(entity.getRiskLevel());
        vo.setAssessedAt(entity.getAssessedAt());
        if (result != null) {
            vo.setScore(result.getScore());
            vo.setDetail(result.getDetail());
        } else if (entity.getResultJson() != null) {
            vo.setDetail(JsonUtils.fromJson(entity.getResultJson(), java.util.Map.class));
        }
        return vo;
    }
}

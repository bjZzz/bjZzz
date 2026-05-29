package com.nanda.integration.writeback;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.integration.config.EndpointConfigService;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.WritebackRequest;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.WritebackResultVO;
import com.nanda.integration.domain.entity.IntEndpointConfig;
import com.nanda.integration.domain.entity.IntWritebackLog;
import com.nanda.integration.mapper.IntWritebackLogMapper;
import com.nanda.integration.service.IntegrationOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WritebackService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final EndpointConfigService endpointConfigService;
    private final WritebackClient writebackClient;
    private final IntWritebackLogMapper intWritebackLogMapper;

    @Transactional
    public WritebackResultVO submit(WritebackRequest request, Long headerOrgId, String apiKey) {
        Long orgId = IntegrationOrgContext.resolveOrgId(headerOrgId);
        validate(request);

        IntEndpointConfig endpoint = endpointConfigService.requireActive(
                EndpointConfigService.TYPE_WRITEBACK, request.getEndpointCode(), orgId);
        endpointConfigService.verifyAccess(endpoint, apiKey, "integration:writeback:execute");

        IntWritebackLog replay = findReplay(request.getClientRequestId(), orgId);
        if (replay != null) {
            WritebackResultVO vo = toVO(replay);
            vo.setIdempotentReplay(true);
            return vo;
        }

        Map<String, Object> payload = sanitizedPayload(request);
        WritebackClient.WritebackCallResult callResult = writebackClient.post(endpoint, payload);

        IntWritebackLog log = new IntWritebackLog();
        log.setId(IdGenerator.nextId());
        log.setEndpointId(endpoint.getId());
        log.setClientRequestId(request.getClientRequestId());
        log.setPayloadJson(JsonUtils.toJson(payload));
        log.setResponseStatus(callResult.getResponseStatus());
        log.setResponseBody(callResult.getResponseBody());
        log.setRetryCount(callResult.getRetryCount());
        log.setStatus(callResult.isSuccess() ? STATUS_SUCCESS : STATUS_FAILED);
        log.setOrgId(orgId);
        log.setCreatedAt(LocalDateTime.now());
        intWritebackLogMapper.insert(log);
        return toVO(log);
    }

    private void validate(WritebackRequest request) {
        if (request == null || request.getEmpiId() == null || !StringUtils.hasText(request.getAssessmentType())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "empiId 和 assessmentType 不能为空");
        }
        if (request.getResultSummary() == null || request.getResultSummary().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "resultSummary 不能为空");
        }
    }

    private IntWritebackLog findReplay(String clientRequestId, Long orgId) {
        if (!StringUtils.hasText(clientRequestId)) {
            return null;
        }
        return intWritebackLogMapper.selectOne(new LambdaQueryWrapper<IntWritebackLog>()
                .eq(IntWritebackLog::getClientRequestId, clientRequestId)
                .eq(IntWritebackLog::getOrgId, orgId)
                .orderByDesc(IntWritebackLog::getCreatedAt)
                .last("LIMIT 1"));
    }

    private Map<String, Object> sanitizedPayload(WritebackRequest request) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("clientRequestId", request.getClientRequestId());
        payload.put("empiId", request.getEmpiId());
        payload.put("assessmentType", request.getAssessmentType());
        payload.put("resultSummary", request.getResultSummary());
        payload.put("reportUrl", request.getReportUrl());
        payload.put("writebackAt", LocalDateTime.now().toString());
        return payload;
    }

    private WritebackResultVO toVO(IntWritebackLog log) {
        WritebackResultVO vo = new WritebackResultVO();
        vo.setLogId(log.getId());
        vo.setClientRequestId(log.getClientRequestId());
        vo.setStatus(log.getStatus());
        vo.setResponseStatus(log.getResponseStatus());
        vo.setResponseBody(log.getResponseBody());
        vo.setRetryCount(log.getRetryCount());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}

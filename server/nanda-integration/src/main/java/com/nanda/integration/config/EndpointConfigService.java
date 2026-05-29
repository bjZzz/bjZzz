package com.nanda.integration.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.EndpointCreateRequest;
import com.nanda.integration.domain.dto.IntegrationW7Dtos.EndpointVO;
import com.nanda.integration.domain.entity.IntEndpointConfig;
import com.nanda.integration.mapper.IntEndpointConfigMapper;
import com.nanda.integration.service.IntegrationOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EndpointConfigService {

    public static final String TYPE_WRITEBACK = "WRITEBACK";
    public static final String TYPE_FHIR = "FHIR";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String AUTH_API_KEY = "API_KEY";

    private final IntEndpointConfigMapper intEndpointConfigMapper;

    public List<EndpointVO> list(String endpointType) {
        Long orgId = IntegrationOrgContext.requireOrgId();
        LambdaQueryWrapper<IntEndpointConfig> wrapper = new LambdaQueryWrapper<IntEndpointConfig>()
                .eq(IntEndpointConfig::getOrgId, orgId)
                .eq(IntEndpointConfig::getDeleted, 0)
                .orderByDesc(IntEndpointConfig::getCreatedAt);
        if (StringUtils.hasText(endpointType)) {
            wrapper.eq(IntEndpointConfig::getEndpointType, endpointType.toUpperCase());
        }
        List<EndpointVO> result = new ArrayList<EndpointVO>();
        for (IntEndpointConfig config : intEndpointConfigMapper.selectList(wrapper)) {
            result.add(toVO(config));
        }
        return result;
    }

    @Transactional
    public EndpointVO create(EndpointCreateRequest request) {
        Long orgId = IntegrationOrgContext.requireOrgId();
        if (!StringUtils.hasText(request.getEndpointCode()) || !StringUtils.hasText(request.getEndpointType())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "端点编码和类型不能为空");
        }
        String endpointType = request.getEndpointType().toUpperCase();
        if (!TYPE_WRITEBACK.equals(endpointType) && !TYPE_FHIR.equals(endpointType)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不支持的端点类型");
        }
        IntEndpointConfig config = new IntEndpointConfig();
        config.setId(IdGenerator.nextId());
        config.setEndpointCode(request.getEndpointCode());
        config.setEndpointType(endpointType);
        config.setBaseUrl(request.getBaseUrl());
        config.setAuthType(StringUtils.hasText(request.getAuthType()) ? request.getAuthType().toUpperCase() : AUTH_API_KEY);
        config.setAuthConfigJson(request.getAuthConfigJson());
        config.setOrgId(orgId);
        config.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : STATUS_ACTIVE);
        config.setCreatedAt(LocalDateTime.now());
        config.setDeleted(0);
        intEndpointConfigMapper.insert(config);
        return toVO(config);
    }

    public IntEndpointConfig requireActive(String endpointType, String endpointCode, Long orgId) {
        LambdaQueryWrapper<IntEndpointConfig> wrapper = new LambdaQueryWrapper<IntEndpointConfig>()
                .eq(IntEndpointConfig::getEndpointType, endpointType)
                .eq(IntEndpointConfig::getOrgId, orgId)
                .eq(IntEndpointConfig::getStatus, STATUS_ACTIVE)
                .eq(IntEndpointConfig::getDeleted, 0)
                .orderByDesc(IntEndpointConfig::getCreatedAt)
                .last("LIMIT 1");
        if (StringUtils.hasText(endpointCode)) {
            wrapper.eq(IntEndpointConfig::getEndpointCode, endpointCode);
        }
        IntEndpointConfig config = intEndpointConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.INTEGRATION_ENDPOINT_DISABLED, "集成端点未启用");
        }
        return config;
    }

    public void verifyAccess(IntEndpointConfig config, String apiKey, String internalPermission) {
        if (IntegrationOrgContext.hasPermission(internalPermission)) {
            return;
        }
        if (!AUTH_API_KEY.equals(config.getAuthType())) {
            return;
        }
        String expected = expectedApiKey(config.getAuthConfigJson());
        if (!StringUtils.hasText(expected) || !expected.equals(apiKey)) {
            throw new BusinessException(ErrorCode.INTEGRATION_AUTH_FAILED, "集成接口认证失败");
        }
    }

    private String expectedApiKey(String authConfigJson) {
        if (!StringUtils.hasText(authConfigJson)) {
            return null;
        }
        Map<String, Object> authConfig = JsonUtils.fromJson(authConfigJson, new TypeReference<Map<String, Object>>() {
        });
        Object apiKey = authConfig.get("apiKey");
        if (apiKey == null) {
            apiKey = authConfig.get("key");
        }
        return apiKey == null ? null : String.valueOf(apiKey);
    }

    private EndpointVO toVO(IntEndpointConfig config) {
        EndpointVO vo = new EndpointVO();
        vo.setId(config.getId());
        vo.setEndpointCode(config.getEndpointCode());
        vo.setEndpointType(config.getEndpointType());
        vo.setBaseUrl(config.getBaseUrl());
        vo.setAuthType(config.getAuthType());
        vo.setOrgId(config.getOrgId());
        vo.setStatus(config.getStatus());
        vo.setCreatedAt(config.getCreatedAt());
        return vo;
    }
}

package com.nanda.integration.writeback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import com.nanda.integration.domain.entity.IntEndpointConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class WritebackClient {

    private static final int MAX_ATTEMPTS = 3;
    private final RestTemplate restTemplate = new RestTemplate();

    public WritebackCallResult post(IntEndpointConfig endpoint, Map<String, Object> payload) {
        if (!StringUtils.hasText(endpoint.getBaseUrl())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "回写端点 baseUrl 不能为空");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        applyOutboundAuth(headers, endpoint.getAuthConfigJson());

        String body = JsonUtils.toJson(payload);
        Exception lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(
                        endpoint.getBaseUrl(), new HttpEntity<String>(body, headers), String.class);
                WritebackCallResult result = new WritebackCallResult();
                result.setResponseStatus(response.getStatusCodeValue());
                result.setResponseBody(response.getBody());
                result.setRetryCount(attempt - 1);
                result.setSuccess(response.getStatusCode().is2xxSuccessful());
                return result;
            } catch (Exception ex) {
                lastError = ex;
                log.warn("Writeback call failed endpoint={} attempt={}", endpoint.getEndpointCode(), attempt, ex);
                sleepBackoff(attempt);
            }
        }
        WritebackCallResult result = new WritebackCallResult();
        result.setResponseStatus(0);
        result.setResponseBody(lastError == null ? "unknown error" : lastError.getMessage());
        result.setRetryCount(MAX_ATTEMPTS - 1);
        result.setSuccess(false);
        return result;
    }

    private void applyOutboundAuth(HttpHeaders headers, String authConfigJson) {
        if (!StringUtils.hasText(authConfigJson)) {
            return;
        }
        Map<String, Object> authConfig = JsonUtils.fromJson(authConfigJson, new TypeReference<Map<String, Object>>() {
        });
        Object apiKey = authConfig.get("outboundApiKey");
        if (apiKey == null) {
            apiKey = authConfig.get("apiKey");
        }
        if (apiKey != null) {
            headers.set("X-Api-Key", String.valueOf(apiKey));
        }
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(100L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Data
    public static class WritebackCallResult {
        private boolean success;
        private Integer responseStatus;
        private String responseBody;
        private Integer retryCount;
    }
}

package com.nanda.analytics.sandbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.config.SandboxProperties;
import com.nanda.common.util.JsonUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RestTemplate BFF 客户端，代理 Python ComputePlane 内部 API。
 * 未启用或调用失败时降级为本地 stub，保证 W8 接口可用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxClient {

    private final SandboxProperties properties;
    private RestTemplate restTemplate;

    public WorkspaceResponse createWorkspace(Long userId, Long orgId, String workspaceId) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("workspaceId", workspaceId);
        body.put("userId", userId);
        body.put("orgId", orgId);
        Map<String, Object> remote = post("/internal/workspace", body, userId, orgId);
        if (remote != null) {
            WorkspaceResponse response = new WorkspaceResponse();
            response.setWorkspaceId(stringVal(remote.get("workspaceId"), workspaceId));
            response.setStatus(stringVal(remote.get("status"), "ACTIVE"));
            response.setKernelStatus(stringVal(remote.get("kernelStatus"), "IDLE"));
            return response;
        }
        WorkspaceResponse stub = new WorkspaceResponse();
        stub.setWorkspaceId(workspaceId);
        stub.setStatus("ACTIVE");
        stub.setKernelStatus("IDLE");
        stub.setLocalStub(true);
        return stub;
    }

    public Map<String, Object> getWorkspace(String workspaceId, Long userId, Long orgId) {
        Map<String, Object> remote = get("/internal/workspace/" + workspaceId, userId, orgId);
        if (remote != null) {
            return remote;
        }
        Map<String, Object> stub = new LinkedHashMap<String, Object>();
        stub.put("workspaceId", workspaceId);
        stub.put("status", "ACTIVE");
        stub.put("kernelStatus", "IDLE");
        stub.put("localStub", true);
        return stub;
    }

    public String getNotebook(String workspaceId, String notebookId, Long userId, Long orgId) {
        Map<String, Object> remote = get("/internal/notebooks/" + workspaceId + "/" + notebookId, userId, orgId);
        if (remote != null && remote.get("content") != null) {
            return String.valueOf(remote.get("content"));
        }
        return "{\"cells\":[],\"metadata\":{},\"nbformat\":4,\"nbformat_minor\":5}";
    }

    public void saveNotebook(String workspaceId, String notebookId, String content, Long userId, Long orgId) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("content", content);
        put("/internal/notebooks/" + workspaceId + "/" + notebookId, body, userId, orgId);
    }

    public Map<String, Object> mountDataset(String datasetId, Long orgId, Long userId, Map<String, Object> mountSpec) {
        Map<String, Object> body = new LinkedHashMap<String, Object>(mountSpec);
        body.put("datasetId", datasetId);
        body.put("orgId", orgId);
        Map<String, Object> remote = post("/internal/datasets/mount", body, userId, orgId);
        if (remote != null) {
            return remote;
        }
        Map<String, Object> stub = new LinkedHashMap<String, Object>();
        stub.put("datasetId", datasetId);
        stub.put("status", "MOUNTED");
        stub.put("localStub", true);
        return stub;
    }

    public List<Map<String, Object>> listRemoteAlgorithms(Long userId, Long orgId) {
        Map<String, Object> remote = get("/internal/algorithms", userId, orgId);
        if (remote != null && remote.get("items") instanceof List) {
            return (List<Map<String, Object>>) remote.get("items");
        }
        return Collections.emptyList();
    }

    public JobResponse submitJob(Map<String, Object> payload, Long userId, Long orgId) {
        Map<String, Object> remote = post("/internal/jobs", payload, userId, orgId);
        if (remote != null) {
            JobResponse response = new JobResponse();
            response.setSandboxJobId(stringVal(remote.get("jobId"), UUID.randomUUID().toString()));
            response.setStatus(stringVal(remote.get("status"), "QUEUED"));
            response.setResult(castMap(remote.get("result")));
            return response;
        }
        JobResponse stub = new JobResponse();
        stub.setSandboxJobId("local-" + UUID.randomUUID());
        stub.setStatus("QUEUED");
        stub.setLocalStub(true);
        return stub;
    }

    public JobResponse getJob(String sandboxJobId, Long userId, Long orgId) {
        Map<String, Object> remote = get("/internal/jobs/" + sandboxJobId, userId, orgId);
        if (remote != null) {
            JobResponse response = new JobResponse();
            response.setSandboxJobId(sandboxJobId);
            response.setStatus(stringVal(remote.get("status"), "QUEUED"));
            response.setResult(castMap(remote.get("result")));
            return response;
        }
        JobResponse stub = new JobResponse();
        stub.setSandboxJobId(sandboxJobId);
        stub.setStatus("QUEUED");
        stub.setLocalStub(true);
        return stub;
    }

    private Map<String, Object> get(String path, Long userId, Long orgId) {
        if (!properties.isEnabled()) {
            return null;
        }
        try {
            ResponseEntity<String> response = restTemplate().exchange(
                    properties.getBaseUrl() + path, HttpMethod.GET, entity(null, userId, orgId), String.class);
            return parseBody(response.getBody());
        } catch (Exception ex) {
            log.warn("Sandbox GET {} failed: {}", path, ex.getMessage());
            return null;
        }
    }

    private Map<String, Object> post(String path, Map<String, Object> body, Long userId, Long orgId) {
        if (!properties.isEnabled()) {
            return null;
        }
        try {
            ResponseEntity<String> response = restTemplate().postForEntity(
                    properties.getBaseUrl() + path, entity(JsonUtils.toJson(body), userId, orgId), String.class);
            return parseBody(response.getBody());
        } catch (Exception ex) {
            log.warn("Sandbox POST {} failed: {}", path, ex.getMessage());
            return null;
        }
    }

    private void put(String path, Map<String, Object> body, Long userId, Long orgId) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            restTemplate().exchange(
                    properties.getBaseUrl() + path,
                    HttpMethod.PUT,
                    entity(JsonUtils.toJson(body), userId, orgId),
                    String.class);
        } catch (Exception ex) {
            log.warn("Sandbox PUT {} failed: {}", path, ex.getMessage());
        }
    }

    private HttpEntity<String> entity(String body, Long userId, Long orgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getInternalToken())) {
            headers.set("X-Internal-Token", properties.getInternalToken());
        }
        if (userId != null) {
            headers.set("X-User-Id", String.valueOf(userId));
        }
        if (orgId != null) {
            headers.set("X-Org-Id", String.valueOf(orgId));
        }
        return new HttpEntity<String>(body, headers);
    }

    private Map<String, Object> parseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        return JsonUtils.fromJson(body, new TypeReference<Map<String, Object>>() {
        });
    }

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(properties.getConnectTimeoutMs());
            factory.setReadTimeout(properties.getReadTimeoutMs());
            restTemplate = new RestTemplate(factory);
        }
        return restTemplate;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private String stringVal(Object value, String defaultValue) {
        return value == null ? defaultValue : String.valueOf(value);
    }

    @Data
    public static class WorkspaceResponse {
        private String workspaceId;
        private String status;
        private String kernelStatus;
        private boolean localStub;
    }

    @Data
    public static class JobResponse {
        private String sandboxJobId;
        private String status;
        private Map<String, Object> result;
        private boolean localStub;
    }
}

package com.nanda.analytics.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AnalyticsW8Dtos {

    private AnalyticsW8Dtos() {
    }

    @Data
    public static class SandboxSessionVO {
        private Long sessionId;
        private String workspaceId;
        private String status;
        private String kernelStatus;
        private LocalDateTime lastActiveAt;
    }

    @Data
    public static class NotebookVO {
        private String notebookId;
        private String content;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class NotebookSaveRequest {
        private String content;
    }

    @Data
    public static class DatasetMountRequest {
        private String sourceType;
        private Long searchQueryId;
        private Integer rowLimit;
    }

    @Data
    public static class DatasetVO {
        private String datasetId;
        private String sourceType;
        private Integer rowCount;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SandboxJobSubmitRequest {
        private String jobType;
        private String methodCode;
        private Map<String, Object> input;
        private String datasetId;
        private String scriptContent;
    }

    @Data
    public static class SandboxJobVO {
        private Long jobId;
        private String sandboxJobId;
        private String status;
        private Map<String, Object> result;
        private LocalDateTime createdAt;
    }

    @Data
    public static class AlgorithmVO {
        private Long id;
        private String algorithmCode;
        private String algorithmName;
        private String version;
        private String packageRef;
        private String status;
    }

    @Data
    public static class AlgorithmRegisterRequest {
        private String algorithmCode;
        private String algorithmName;
        private String version;
        private String packageRef;
    }

    @Data
    public static class ScriptTemplateCreateRequest {
        private String templateCode;
        private String templateName;
        private String scriptContent;
    }

    @Data
    public static class ScriptTemplateUpdateRequest {
        private String templateName;
        private String scriptContent;
    }

    @Data
    public static class ScriptTemplateVO {
        private Long id;
        private String templateCode;
        private String templateName;
        private String scriptContent;
        private Long orgId;
        private LocalDateTime createdAt;
    }
}

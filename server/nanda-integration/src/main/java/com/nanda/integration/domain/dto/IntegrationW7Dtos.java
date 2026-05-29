package com.nanda.integration.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class IntegrationW7Dtos {

    private IntegrationW7Dtos() {
    }

    @Data
    public static class EndpointCreateRequest {
        private String endpointCode;
        private String endpointType;
        private String baseUrl;
        private String authType;
        private String authConfigJson;
        private String status;
    }

    @Data
    public static class EndpointVO {
        private Long id;
        private String endpointCode;
        private String endpointType;
        private String baseUrl;
        private String authType;
        private Long orgId;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class WritebackRequest {
        private String endpointCode;
        private String clientRequestId;
        private Long empiId;
        private String assessmentType;
        private Map<String, Object> resultSummary;
        private String reportUrl;
    }

    @Data
    public static class WritebackResultVO {
        private Long logId;
        private String clientRequestId;
        private String status;
        private Integer responseStatus;
        private String responseBody;
        private Integer retryCount;
        private LocalDateTime createdAt;
        private boolean idempotentReplay;
    }

    @Data
    public static class UploadResultVO {
        private Long uploadBatchId;
        private Long stgBatchId;
        private String templateType;
        private String fileName;
        private String clientRequestId;
        private Integer totalRows;
        private Integer successRows;
        private Integer failRows;
        private String status;
        private LocalDateTime createdAt;
        private List<UploadErrorVO> errors;
    }

    @Data
    public static class UploadErrorVO {
        private Integer row;
        private String message;
        private String rowDataJson;
    }
}

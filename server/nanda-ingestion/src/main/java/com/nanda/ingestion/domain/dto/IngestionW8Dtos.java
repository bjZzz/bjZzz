package com.nanda.ingestion.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;

public final class IngestionW8Dtos {

    private IngestionW8Dtos() {
    }

    @Data
    public static class WebhookSubscriptionCreateRequest {
        @NotBlank
        private String endpointUrl;
    }

    @Data
    public static class WebhookSubscriptionVO {
        private Long id;
        private String endpointUrl;
        private String receiveUrl;
        private String secret;
        private Long orgId;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class WebhookReceiveResultVO {
        private Long batchId;
        private Integer recordCount;
        private String status;
    }

    @Data
    public static class DicomUploadResultVO {
        private Long batchId;
        private String studyInstanceUid;
        private String patientId;
        private String modality;
        private Map<String, Object> metadata;
    }

    @Data
    public static class DicomMetadataRequest {
        private String patientId;
        private String studyInstanceUid;
        private String seriesInstanceUid;
        private String sopInstanceUid;
        private String modality;
        private String studyDate;
        private Map<String, Object> extra;
    }
}

package com.nanda.analytics.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AnalyticsW6Dtos {

    private AnalyticsW6Dtos() {
    }

    @Data
    public static class RiskAssessRequest {
        private Long empiId;
        private Map<String, Object> input;
    }

    @Data
    public static class RiskResultVO {
        private Long id;
        private String modelCode;
        private Double score;
        private String riskLevel;
        private Map<String, Object> detail;
        private LocalDateTime assessedAt;
    }

    @Data
    public static class StatRequest {
        private Map<String, Object> input;
        private boolean persist;
    }

    @Data
    public static class StatResultVO {
        private Long jobId;
        private String method;
        private Map<String, Object> result;
    }

    @Data
    public static class ReportCreateRequest {
        private Long empiId;
        private String modelCode;
        private String title;
        private Map<String, Object> input;
    }

    @Data
    public static class ReportVO {
        private Long id;
        private String reportType;
        private Long sourceId;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ReportDownloadVO {
        private String fileName;
        private String contentType;
        private byte[] content;
    }

    @Data
    public static class DashboardCreateRequest {
        private String dashboardName;
        private String configJson;
    }

    @Data
    public static class DashboardVO {
        private Long id;
        private String dashboardName;
        private String configJson;
        private LocalDateTime createdAt;
    }

    @Data
    public static class DashboardDataVO {
        private Map<String, Object> metrics;
        private List<Map<String, Object>> series;
    }
}

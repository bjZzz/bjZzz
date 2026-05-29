package com.nanda.analytics.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    @Data
    public static class SearchExecuteRequest {
        private String queryName;
        private String queryJson;
        private Integer page;
        private Integer size;
    }

    @Data
    public static class SearchResultVO {
        private List<SearchHitVO> items;
        private long total;
        private int page;
        private int size;
    }

    @Data
    public static class SearchHitVO {
        private Long empiId;
        private String specialtyTypes;
        private String diagnosisCodes;
        private String demographics;
        private String completenessScore;
    }

    @Data
    public static class CountNodeRequest {
        private String queryJson;
    }

    @Data
    public static class CountNodeVO {
        private String field;
        private String value;
        private long count;
    }

    @Data
    public static class ExportCreateRequest {
        private Long searchQueryId;
        private String queryJson;
        private String exportFormat;
        private String exportScopeJson;
    }

    @Data
    public static class ExportTaskVO {
        private Long id;
        private Long searchQueryId;
        private String exportFormat;
        private String status;
        private Long approverId;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ExportApproveRequest {
        private String comment;
    }

    @Data
    public static class ExportRejectRequest {
        private String reason;
    }

    @Data
    public static class ExportDownloadVO {
        private String fileName;
        private String contentType;
        private byte[] content;
    }

    @Data
    public static class ImportToProjectRequest {
        private Long exportTaskId;
        private Long cohortId;
        private String groupLabel;
    }

    @Data
    public static class ImportToProjectResultVO {
        private int enrolled;
    }

    @Data
    public static class ParsedSearchQuery {
        private String operator;
        private List<Map<String, Object>> conditions;
    }
}

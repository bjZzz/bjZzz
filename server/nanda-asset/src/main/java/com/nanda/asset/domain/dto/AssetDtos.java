package com.nanda.asset.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class AssetDtos {

    private AssetDtos() {
    }

    @Data
    public static class EmpiPatientVO {
        private Long id;
        private String displayName;
        private String gender;
        private LocalDate birthDate;
        private String mergeStatus;
        private BigDecimal matchConfidence;
    }

    @Data
    public static class EmpiMatchRequest {
        private String sourceRef;
        private String name;
        private String gender;
        private String birthDate;
        private String phone;
        private String address;
        private String idCard;
        private String sourceSystem;
    }

    @Data
    public static class EmpiMatchResultVO {
        private Long empiId;
        private String matchType;
        private BigDecimal confidence;
        private Long candidateId;
    }

    @Data
    public static class EmpiMatchCandidateVO {
        private Long id;
        private Long candidateEmpiId;
        private String candidateName;
        private BigDecimal matchScore;
        private String matchFeatures;
        private String reviewStatus;
        private LocalDateTime createdAt;
    }

    @Data
    public static class TimelineEventVO {
        private String eventType;
        private String title;
        private String detail;
        private LocalDateTime eventTime;
        private Long sourceId;
    }

    @Data
    public static class EmpiMatchRuleVO {
        private Long id;
        private String ruleName;
        private String ruleConfigJson;
        private String status;
    }

    @Data
    public static class SpecialtyPatientVO {
        private Long id;
        private Long empiId;
        private String specialtyType;
        private String displayName;
        private String status;
        private String coreFields;
        private String extendedFields;
        private LocalDate firstDiagnosisDate;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SpecialtyOverviewVO {
        private String specialtyType;
        private long totalPatients;
        private long activePatients;
        private long pendingCandidates;
    }

    @Data
    public static class Patient360VO {
        private SpecialtyPatientVO patient;
        private EmpiPatientVO empi;
        private List<TimelineEventVO> timeline;
        private List<Map<String, Object>> labResults;
        private List<Map<String, Object>> treatments;
        private List<Map<String, Object>> followUps;
        private List<Map<String, Object>> medicalRecords;
    }

    @Data
    public static class CockpitSummaryVO {
        private long totalPatients;
        private long pendingMatchCandidates;
        private long openReviewTasks;
        private long comorbidityViews;
        private List<SpecialtyOverviewVO> specialtyOverviews;
    }

    @Data
    public static class ComorbidityRuleVO {
        private Long id;
        private String ruleName;
        private String expressionJson;
        private String status;
    }

    @Data
    public static class ComorbidityRuleCreateRequest {
        private String ruleName;
        private String expressionJson;
        private String timeWindowJson;
    }

    @Data
    public static class ComorbidityViewVO {
        private Long id;
        private Long ruleId;
        private String ruleName;
        private Long empiId;
        private String displayName;
        private String comorbidityLabels;
        private LocalDateTime refreshedAt;
    }

    @Data
    public static class ComorbidityPatientDetailVO {
        private Long empiId;
        private String displayName;
        private List<SpecialtyPatientVO> specialtyRecords;
        private String comorbidityLabels;
    }

    @Data
    public static class KnowledgeDocumentVO {
        private Long id;
        private String title;
        private String docType;
        private String fileRef;
        private List<String> authors;
        private List<String> tags;
        private LocalDateTime createdAt;
    }

    @Data
    public static class KnowledgeImportRequest {
        private String title;
        private String docType;
        private String fileRef;
        private List<String> authors;
        private List<String> tags;
    }

    @Data
    public static class QcDashboardVO {
        private List<QcMetricVO> metrics;
        private long openReviewTasks;
        private long pendingCandidates;
    }

    @Data
    public static class QcMetricVO {
        private String metricType;
        private BigDecimal metricValue;
        private BigDecimal threshold;
        private boolean alert;
    }

    @Data
    public static class QcSampleBatchCreateRequest {
        private String batchName;
        private int sampleSize;
        private String specialtyType;
    }

    @Data
    public static class QcSampleBatchVO {
        private Long id;
        private String batchName;
        private int sampleCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class QcReviewTaskVO {
        private Long id;
        private Long sampleRecordId;
        private Long patientId;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class QcReviewRequest {
        private String decision;
        private String comment;
    }

    @Data
    public static class DualScreenSupplementRequest {
        private Long patientId;
        private String fieldCode;
        private String fieldValue;
        private String sourceSnapshotJson;
    }

    @Data
    public static class SupplementResultVO {
        private Long changeLogId;
        private Long patientId;
        private String fieldCode;
        private String status;
    }
}

package com.nanda.research.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ResearchDtos {

    private ResearchDtos() {
    }

    @Data
    public static class ProjectCreateRequest {
        private String projectName;
        private String designJson;
        private String templateCode;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class ProjectTransitionRequest {
        private String targetStatus;
    }

    @Data
    public static class ProjectVO {
        private Long id;
        private String projectCode;
        private String projectName;
        private String status;
        private String designJson;
        private String templateCode;
        private Long piUserId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ProjectMemberVO {
        private Long id;
        private Long projectId;
        private Long userId;
        private String roleInProject;
    }

    @Data
    public static class ProjectMemberAddRequest {
        private Long userId;
        private String roleInProject;
    }

    @Data
    public static class CohortCreateRequest {
        private Long projectId;
        private String cohortName;
        private String cohortType;
        private String ruleJson;
    }

    @Data
    public static class CohortVO {
        private Long id;
        private Long projectId;
        private String cohortName;
        private String cohortType;
        private String ruleJson;
        private Integer memberCount;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CohortMemberEnrollRequest {
        private Long empiId;
        private String groupLabel;
    }

    @Data
    public static class CohortMemberVO {
        private Long id;
        private Long cohortId;
        private Long empiId;
        private String groupLabel;
        private LocalDate enrollDate;
        private String status;
    }

    @Data
    public static class CohortScreenResultVO {
        private int screened;
        private int enrolled;
    }

    @Data
    public static class FollowUpPlanCreateRequest {
        private Long projectId;
        private String planName;
        private List<FollowUpStageRequest> stages;
    }

    @Data
    public static class FollowUpStageRequest {
        private String stageName;
        private Integer offsetDays;
        private Integer windowDays;
        private Integer sortOrder;
    }

    @Data
    public static class FollowUpPlanVO {
        private Long id;
        private Long projectId;
        private String planName;
        private List<FollowUpStageVO> stages;
    }

    @Data
    public static class FollowUpStageVO {
        private Long id;
        private String stageName;
        private Integer offsetDays;
        private Integer windowDays;
        private Integer sortOrder;
    }

    @Data
    public static class FollowUpTaskVO {
        private Long id;
        private Long stageId;
        private Long cohortMemberId;
        private LocalDate dueDate;
        private String status;
        private String channel;
    }

    @Data
    public static class ProjectProgressVO {
        private Long projectId;
        private int cohortCount;
        private int memberCount;
        private int pendingTasks;
        private int overdueTasks;
    }
}

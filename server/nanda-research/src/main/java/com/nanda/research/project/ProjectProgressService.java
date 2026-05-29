package com.nanda.research.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.research.domain.dto.ResearchDtos.ProjectProgressVO;
import com.nanda.research.domain.entity.ResCohort;
import com.nanda.research.domain.entity.ResCohortMember;
import com.nanda.research.domain.entity.ResFollowUpPlan;
import com.nanda.research.domain.entity.ResFollowUpStage;
import com.nanda.research.domain.entity.ResFollowUpTask;
import com.nanda.research.mapper.ResCohortMapper;
import com.nanda.research.mapper.ResCohortMemberMapper;
import com.nanda.research.mapper.ResFollowUpPlanMapper;
import com.nanda.research.mapper.ResFollowUpStageMapper;
import com.nanda.research.mapper.ResFollowUpTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectProgressService {

    private final ProjectService projectService;
    private final ResCohortMapper resCohortMapper;
    private final ResCohortMemberMapper resCohortMemberMapper;
    private final ResFollowUpPlanMapper resFollowUpPlanMapper;
    private final ResFollowUpStageMapper resFollowUpStageMapper;
    private final ResFollowUpTaskMapper resFollowUpTaskMapper;

    public ProjectProgressVO getProgress(Long projectId) {
        projectService.requireProject(projectId);
        List<ResCohort> cohorts = resCohortMapper.selectList(new LambdaQueryWrapper<ResCohort>()
                .eq(ResCohort::getProjectId, projectId)
                .eq(ResCohort::getDeleted, 0));

        int memberCount = 0;
        for (ResCohort cohort : cohorts) {
            Long count = resCohortMemberMapper.selectCount(new LambdaQueryWrapper<ResCohortMember>()
                    .eq(ResCohortMember::getCohortId, cohort.getId())
                    .eq(ResCohortMember::getStatus, "ACTIVE"));
            memberCount += count != null ? count.intValue() : 0;
        }

        Set<Long> stageIds = resolveStageIds(projectId);
        int pendingTasks = 0;
        int overdueTasks = 0;
        if (!stageIds.isEmpty()) {
            List<ResFollowUpTask> tasks = resFollowUpTaskMapper.selectList(new LambdaQueryWrapper<ResFollowUpTask>()
                    .in(ResFollowUpTask::getStageId, stageIds));
            LocalDate today = LocalDate.now();
            for (ResFollowUpTask task : tasks) {
                if ("PENDING".equals(task.getStatus()) || "IN_PROGRESS".equals(task.getStatus())) {
                    pendingTasks++;
                    if (task.getDueDate() != null && task.getDueDate().isBefore(today)) {
                        overdueTasks++;
                    }
                }
            }
        }

        ProjectProgressVO vo = new ProjectProgressVO();
        vo.setProjectId(projectId);
        vo.setCohortCount(cohorts.size());
        vo.setMemberCount(memberCount);
        vo.setPendingTasks(pendingTasks);
        vo.setOverdueTasks(overdueTasks);
        return vo;
    }

    private Set<Long> resolveStageIds(Long projectId) {
        List<ResFollowUpPlan> plans = resFollowUpPlanMapper.selectList(new LambdaQueryWrapper<ResFollowUpPlan>()
                .eq(ResFollowUpPlan::getProjectId, projectId));
        Set<Long> stageIds = new HashSet<Long>();
        for (ResFollowUpPlan plan : plans) {
            List<ResFollowUpStage> stages = resFollowUpStageMapper.selectList(new LambdaQueryWrapper<ResFollowUpStage>()
                    .eq(ResFollowUpStage::getPlanId, plan.getId()));
            for (ResFollowUpStage stage : stages) {
                stageIds.add(stage.getId());
            }
        }
        return stageIds;
    }
}

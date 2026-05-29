package com.nanda.research.followup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpPlanCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpPlanVO;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpStageRequest;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpStageVO;
import com.nanda.research.domain.dto.ResearchDtos.FollowUpTaskVO;
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
import com.nanda.research.project.ProjectService;
import com.nanda.research.service.ResearchOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowUpTaskService {

    private final ResFollowUpPlanMapper resFollowUpPlanMapper;
    private final ResFollowUpStageMapper resFollowUpStageMapper;
    private final ResFollowUpTaskMapper resFollowUpTaskMapper;
    private final ResCohortMapper resCohortMapper;
    private final ResCohortMemberMapper resCohortMemberMapper;
    private final ProjectService projectService;

    public List<FollowUpPlanVO> listPlans(Long projectId) {
        Long orgId = ResearchOrgContext.requireOrgId();
        LambdaQueryWrapper<ResFollowUpPlan> wrapper = new LambdaQueryWrapper<ResFollowUpPlan>()
                .eq(ResFollowUpPlan::getOrgId, orgId);
        if (projectId != null) {
            wrapper.eq(ResFollowUpPlan::getProjectId, projectId);
        }
        List<ResFollowUpPlan> plans = resFollowUpPlanMapper.selectList(wrapper);
        List<FollowUpPlanVO> result = new ArrayList<FollowUpPlanVO>();
        for (ResFollowUpPlan plan : plans) {
            result.add(toPlanVO(plan));
        }
        return result;
    }

    @Transactional
    public FollowUpPlanVO createPlan(FollowUpPlanCreateRequest request) {
        projectService.requireProject(request.getProjectId());
        Long orgId = ResearchOrgContext.requireOrgId();

        ResFollowUpPlan plan = new ResFollowUpPlan();
        plan.setId(IdGenerator.nextId());
        plan.setProjectId(request.getProjectId());
        plan.setPlanName(request.getPlanName());
        plan.setOrgId(orgId);
        plan.setCreatedAt(LocalDateTime.now());
        resFollowUpPlanMapper.insert(plan);

        if (request.getStages() != null) {
            for (FollowUpStageRequest stageReq : request.getStages()) {
                ResFollowUpStage stage = new ResFollowUpStage();
                stage.setId(IdGenerator.nextId());
                stage.setPlanId(plan.getId());
                stage.setStageName(stageReq.getStageName());
                stage.setOffsetDays(stageReq.getOffsetDays());
                stage.setWindowDays(stageReq.getWindowDays());
                stage.setSortOrder(stageReq.getSortOrder());
                resFollowUpStageMapper.insert(stage);
            }
        }
        generateTasksForPlan(plan.getId());
        return toPlanVO(plan);
    }

    @Transactional
    public void generateTasksForPlan(Long planId) {
        ResFollowUpPlan plan = resFollowUpPlanMapper.selectById(planId);
        if (plan == null) {
            return;
        }
        List<ResFollowUpStage> stages = resFollowUpStageMapper.selectList(new LambdaQueryWrapper<ResFollowUpStage>()
                .eq(ResFollowUpStage::getPlanId, planId)
                .orderByAsc(ResFollowUpStage::getSortOrder));

        List<ResCohort> cohorts = resCohortMapper.selectList(new LambdaQueryWrapper<ResCohort>()
                .eq(ResCohort::getProjectId, plan.getProjectId())
                .eq(ResCohort::getDeleted, 0));
        for (ResCohort cohort : cohorts) {
            List<ResCohortMember> members = resCohortMemberMapper.selectList(new LambdaQueryWrapper<ResCohortMember>()
                    .eq(ResCohortMember::getCohortId, cohort.getId())
                    .eq(ResCohortMember::getStatus, "ACTIVE"));
            for (ResCohortMember member : members) {
                LocalDate baseDate = member.getEnrollDate() != null ? member.getEnrollDate() : LocalDate.now();
                for (ResFollowUpStage stage : stages) {
                    createTaskIfAbsent(stage, member, baseDate);
                }
            }
        }
    }

    public PageResult<FollowUpTaskVO> listTasks(PageQuery query, String status, LocalDate dueBefore, Long projectId) {
        Set<Long> stageIds = resolveStageIds(projectId);
        if (stageIds.isEmpty()) {
            return PageResult.of(new ArrayList<FollowUpTaskVO>(), query.getPage(), query.getSize(), 0);
        }
        LambdaQueryWrapper<ResFollowUpTask> wrapper = new LambdaQueryWrapper<ResFollowUpTask>()
                .in(ResFollowUpTask::getStageId, stageIds)
                .orderByAsc(ResFollowUpTask::getDueDate);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ResFollowUpTask::getStatus, status);
        }
        if (dueBefore != null) {
            wrapper.le(ResFollowUpTask::getDueDate, dueBefore);
        }
        Page<ResFollowUpTask> page = resFollowUpTaskMapper.selectPage(
                new Page<ResFollowUpTask>(query.getPage(), query.getSize()), wrapper);
        List<FollowUpTaskVO> items = new ArrayList<FollowUpTaskVO>();
        for (ResFollowUpTask task : page.getRecords()) {
            items.add(toTaskVO(task));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    @Transactional
    public FollowUpTaskVO startTask(Long taskId) {
        ResFollowUpTask task = requireTask(taskId);
        if (!"PENDING".equals(task.getStatus()) && !"OVERDUE".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "任务状态不允许启动");
        }
        task.setStatus("IN_PROGRESS");
        resFollowUpTaskMapper.updateById(task);
        return toTaskVO(task);
    }

    @Transactional
    public FollowUpTaskVO completeTask(Long taskId, String channel) {
        ResFollowUpTask task = requireTask(taskId);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        if (channel != null) {
            task.setChannel(channel);
        }
        resFollowUpTaskMapper.updateById(task);
        return toTaskVO(task);
    }

    @Transactional
    public int markOverdueTasks() {
        List<ResFollowUpTask> tasks = resFollowUpTaskMapper.selectList(new LambdaQueryWrapper<ResFollowUpTask>()
                .in(ResFollowUpTask::getStatus, "PENDING", "IN_PROGRESS")
                .lt(ResFollowUpTask::getDueDate, LocalDate.now()));
        for (ResFollowUpTask task : tasks) {
            task.setStatus("OVERDUE");
            resFollowUpTaskMapper.updateById(task);
        }
        return tasks.size();
    }

    public List<FollowUpTaskVO> listDueReminders(int daysAhead) {
        LocalDate target = LocalDate.now().plusDays(daysAhead);
        List<ResFollowUpTask> tasks = resFollowUpTaskMapper.selectList(new LambdaQueryWrapper<ResFollowUpTask>()
                .in(ResFollowUpTask::getStatus, "PENDING", "IN_PROGRESS")
                .eq(ResFollowUpTask::getDueDate, target));
        List<FollowUpTaskVO> result = new ArrayList<FollowUpTaskVO>();
        for (ResFollowUpTask task : tasks) {
            result.add(toTaskVO(task));
        }
        return result;
    }

    private void createTaskIfAbsent(ResFollowUpStage stage, ResCohortMember member, LocalDate baseDate) {
        ResFollowUpTask existing = resFollowUpTaskMapper.selectOne(new LambdaQueryWrapper<ResFollowUpTask>()
                .eq(ResFollowUpTask::getStageId, stage.getId())
                .eq(ResFollowUpTask::getCohortMemberId, member.getId()));
        if (existing != null) {
            return;
        }
        ResFollowUpTask task = new ResFollowUpTask();
        task.setId(IdGenerator.nextId());
        task.setStageId(stage.getId());
        task.setCohortMemberId(member.getId());
        int offset = stage.getOffsetDays() != null ? stage.getOffsetDays() : 0;
        task.setDueDate(baseDate.plusDays(offset));
        task.setStatus("PENDING");
        resFollowUpTaskMapper.insert(task);
    }

    private ResFollowUpTask requireTask(Long taskId) {
        ResFollowUpTask task = resFollowUpTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "随访任务不存在");
        }
        return task;
    }

    private Set<Long> resolveStageIds(Long projectId) {
        Long orgId = ResearchOrgContext.requireOrgId();
        LambdaQueryWrapper<ResFollowUpPlan> planWrapper = new LambdaQueryWrapper<ResFollowUpPlan>()
                .eq(ResFollowUpPlan::getOrgId, orgId);
        if (projectId != null) {
            planWrapper.eq(ResFollowUpPlan::getProjectId, projectId);
        }
        List<ResFollowUpPlan> plans = resFollowUpPlanMapper.selectList(planWrapper);
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

    private FollowUpPlanVO toPlanVO(ResFollowUpPlan plan) {
        FollowUpPlanVO vo = new FollowUpPlanVO();
        vo.setId(plan.getId());
        vo.setProjectId(plan.getProjectId());
        vo.setPlanName(plan.getPlanName());
        List<ResFollowUpStage> stages = resFollowUpStageMapper.selectList(new LambdaQueryWrapper<ResFollowUpStage>()
                .eq(ResFollowUpStage::getPlanId, plan.getId())
                .orderByAsc(ResFollowUpStage::getSortOrder));
        List<FollowUpStageVO> stageVos = new ArrayList<FollowUpStageVO>();
        for (ResFollowUpStage stage : stages) {
            FollowUpStageVO stageVO = new FollowUpStageVO();
            stageVO.setId(stage.getId());
            stageVO.setStageName(stage.getStageName());
            stageVO.setOffsetDays(stage.getOffsetDays());
            stageVO.setWindowDays(stage.getWindowDays());
            stageVO.setSortOrder(stage.getSortOrder());
            stageVos.add(stageVO);
        }
        vo.setStages(stageVos);
        return vo;
    }

    private FollowUpTaskVO toTaskVO(ResFollowUpTask task) {
        FollowUpTaskVO vo = new FollowUpTaskVO();
        vo.setId(task.getId());
        vo.setStageId(task.getStageId());
        vo.setCohortMemberId(task.getCohortMemberId());
        vo.setDueDate(task.getDueDate());
        vo.setStatus(task.getStatus());
        vo.setChannel(task.getChannel());
        return vo;
    }
}

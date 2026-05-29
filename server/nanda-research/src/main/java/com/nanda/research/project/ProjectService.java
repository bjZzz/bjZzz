package com.nanda.research.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.domain.dto.ResearchDtos.ProjectCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectTransitionRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectVO;
import com.nanda.research.domain.entity.ResProject;
import com.nanda.research.mapper.ResProjectMapper;
import com.nanda.research.service.ResearchOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ResProjectMapper resProjectMapper;
    private final ProjectStateMachine projectStateMachine;

    public PageResult<ProjectVO> list(PageQuery query) {
        Long orgId = ResearchOrgContext.requireOrgId();
        Page<ResProject> page = resProjectMapper.selectPage(
                new Page<ResProject>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<ResProject>()
                        .eq(ResProject::getOrgId, orgId)
                        .eq(ResProject::getDeleted, 0)
                        .orderByDesc(ResProject::getCreatedAt));
        List<ProjectVO> items = new ArrayList<ProjectVO>();
        for (ResProject project : page.getRecords()) {
            items.add(toVO(project));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public ProjectVO get(Long id) {
        return toVO(requireProject(id));
    }

    @Transactional
    public ProjectVO create(ProjectCreateRequest request) {
        Long orgId = ResearchOrgContext.requireOrgId();
        Long userId = ResearchOrgContext.currentUserId();
        ResProject project = new ResProject();
        project.setId(IdGenerator.nextId());
        project.setProjectCode("PRJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        project.setProjectName(request.getProjectName());
        project.setDesignJson(request.getDesignJson());
        project.setTemplateCode(request.getTemplateCode());
        project.setStatus("DRAFT");
        project.setPiUserId(userId);
        project.setOrgId(orgId);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedBy(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project.setDeleted(0);
        resProjectMapper.insert(project);
        return toVO(project);
    }

    @Transactional
    public ProjectVO transition(Long id, ProjectTransitionRequest request) {
        ResProject project = requireProject(id);
        projectStateMachine.validate(project.getStatus(), request.getTargetStatus());
        project.setStatus(request.getTargetStatus());
        if ("ARCHIVED".equals(request.getTargetStatus())) {
            project.setArchivedAt(LocalDateTime.now());
        }
        project.setUpdatedAt(LocalDateTime.now());
        resProjectMapper.updateById(project);
        return toVO(project);
    }

    public ResProject requireProject(Long id) {
        ResProject project = resProjectMapper.selectById(id);
        if (project == null || project.getDeleted() != null && project.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "科研项目不存在");
        }
        if (!project.getOrgId().equals(ResearchOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该项目");
        }
        return project;
    }

    private ProjectVO toVO(ResProject project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectCode(project.getProjectCode());
        vo.setProjectName(project.getProjectName());
        vo.setStatus(project.getStatus());
        vo.setDesignJson(project.getDesignJson());
        vo.setTemplateCode(project.getTemplateCode());
        vo.setPiUserId(project.getPiUserId());
        vo.setStartDate(project.getStartDate());
        vo.setEndDate(project.getEndDate());
        vo.setCreatedAt(project.getCreatedAt());
        return vo;
    }
}

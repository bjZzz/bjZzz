package com.nanda.research.project;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.domain.dto.ResearchDtos.ProjectMemberAddRequest;
import com.nanda.research.domain.dto.ResearchDtos.ProjectMemberVO;
import com.nanda.research.domain.entity.ResProjectMember;
import com.nanda.research.mapper.ResProjectMemberMapper;
import com.nanda.research.service.ResearchOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ResProjectMemberMapper resProjectMemberMapper;
    private final ProjectService projectService;

    public List<ProjectMemberVO> listMembers(Long projectId) {
        projectService.requireProject(projectId);
        List<ResProjectMember> members = resProjectMemberMapper.selectList(new LambdaQueryWrapper<ResProjectMember>()
                .eq(ResProjectMember::getProjectId, projectId)
                .eq(ResProjectMember::getDeleted, 0));
        List<ProjectMemberVO> result = new ArrayList<ProjectMemberVO>();
        for (ResProjectMember member : members) {
            result.add(toVO(member));
        }
        return result;
    }

    @Transactional
    public ProjectMemberVO addMember(Long projectId, ProjectMemberAddRequest request) {
        projectService.requireProject(projectId);
        Long orgId = ResearchOrgContext.requireOrgId();
        ResProjectMember existing = resProjectMemberMapper.selectOne(new LambdaQueryWrapper<ResProjectMember>()
                .eq(ResProjectMember::getProjectId, projectId)
                .eq(ResProjectMember::getUserId, request.getUserId())
                .eq(ResProjectMember::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户已是项目成员");
        }
        ResProjectMember member = new ResProjectMember();
        member.setId(IdGenerator.nextId());
        member.setProjectId(projectId);
        member.setUserId(request.getUserId());
        member.setRoleInProject(request.getRoleInProject());
        member.setOrgId(orgId);
        member.setDeleted(0);
        resProjectMemberMapper.insert(member);
        return toVO(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long memberId) {
        projectService.requireProject(projectId);
        ResProjectMember member = resProjectMemberMapper.selectById(memberId);
        if (member == null || member.getDeleted() != null && member.getDeleted() == 1
                || !member.getProjectId().equals(projectId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目成员不存在");
        }
        resProjectMemberMapper.deleteById(memberId);
    }

    private ProjectMemberVO toVO(ResProjectMember member) {
        ProjectMemberVO vo = new ProjectMemberVO();
        vo.setId(member.getId());
        vo.setProjectId(member.getProjectId());
        vo.setUserId(member.getUserId());
        vo.setRoleInProject(member.getRoleInProject());
        return vo;
    }
}

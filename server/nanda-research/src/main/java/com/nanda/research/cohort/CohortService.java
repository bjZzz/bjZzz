package com.nanda.research.cohort;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.research.domain.dto.ResearchDtos.CohortCreateRequest;
import com.nanda.research.domain.dto.ResearchDtos.CohortMemberEnrollRequest;
import com.nanda.research.domain.dto.ResearchDtos.CohortMemberVO;
import com.nanda.research.domain.dto.ResearchDtos.CohortScreenResultVO;
import com.nanda.research.domain.dto.ResearchDtos.CohortVO;
import com.nanda.research.domain.entity.ResCohort;
import com.nanda.research.domain.entity.ResCohortMember;
import com.nanda.research.domain.entity.ResIdxSearchDocument;
import com.nanda.research.mapper.ResCohortMapper;
import com.nanda.research.mapper.ResCohortMemberMapper;
import com.nanda.research.mapper.ResIdxSearchDocumentMapper;
import com.nanda.research.project.ProjectService;
import com.nanda.research.service.ResearchOrgContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CohortService {

    private final ResCohortMapper resCohortMapper;
    private final ResCohortMemberMapper resCohortMemberMapper;
    private final ResIdxSearchDocumentMapper resIdxSearchDocumentMapper;
    private final ProjectService projectService;
    private final CohortRuleEngine cohortRuleEngine;

    public PageResult<CohortVO> list(PageQuery query, Long projectId) {
        Long orgId = ResearchOrgContext.requireOrgId();
        LambdaQueryWrapper<ResCohort> wrapper = new LambdaQueryWrapper<ResCohort>()
                .eq(ResCohort::getOrgId, orgId)
                .eq(ResCohort::getDeleted, 0)
                .orderByDesc(ResCohort::getCreatedAt);
        if (projectId != null) {
            wrapper.eq(ResCohort::getProjectId, projectId);
        }
        Page<ResCohort> page = resCohortMapper.selectPage(new Page<ResCohort>(query.getPage(), query.getSize()), wrapper);
        List<CohortVO> items = new ArrayList<CohortVO>();
        for (ResCohort cohort : page.getRecords()) {
            items.add(toVO(cohort));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public CohortVO get(Long id) {
        return toVO(requireCohort(id));
    }

    @Transactional
    public CohortVO create(CohortCreateRequest request) {
        projectService.requireProject(request.getProjectId());
        Long orgId = ResearchOrgContext.requireOrgId();
        ResCohort cohort = new ResCohort();
        cohort.setId(IdGenerator.nextId());
        cohort.setProjectId(request.getProjectId());
        cohort.setCohortName(request.getCohortName());
        cohort.setCohortType(request.getCohortType());
        cohort.setRuleJson(request.getRuleJson());
        cohort.setMemberCount(0);
        cohort.setOrgId(orgId);
        cohort.setCreatedAt(LocalDateTime.now());
        cohort.setDeleted(0);
        resCohortMapper.insert(cohort);
        return toVO(cohort);
    }

    @Transactional
    public CohortVO updateRules(Long id, String ruleJson) {
        ResCohort cohort = requireCohort(id);
        cohort.setRuleJson(ruleJson);
        resCohortMapper.updateById(cohort);
        return toVO(cohort);
    }

    @Transactional
    public CohortScreenResultVO screen(Long cohortId) {
        ResCohort cohort = requireCohort(cohortId);
        Long orgId = ResearchOrgContext.requireOrgId();
        List<ResIdxSearchDocument> documents = resIdxSearchDocumentMapper.selectList(
                new LambdaQueryWrapper<ResIdxSearchDocument>().eq(ResIdxSearchDocument::getOrgId, orgId));

        int screened = documents.size();
        int enrolled = 0;
        for (ResIdxSearchDocument document : documents) {
            if (cohortRuleEngine.evaluate(cohort.getRuleJson(), document)) {
                if (enrollIfAbsent(cohortId, document.getEmpiId(), null)) {
                    enrolled++;
                }
            }
        }
        refreshMemberCount(cohortId);
        CohortScreenResultVO result = new CohortScreenResultVO();
        result.setScreened(screened);
        result.setEnrolled(enrolled);
        return result;
    }

    @Transactional
    public int batchEnroll(Long cohortId, List<Long> empiIds, String groupLabel) {
        requireCohort(cohortId);
        int enrolled = 0;
        for (Long empiId : empiIds) {
            if (enrollIfAbsent(cohortId, empiId, groupLabel)) {
                enrolled++;
            }
        }
        refreshMemberCount(cohortId);
        return enrolled;
    }

    @Transactional
    public CohortMemberVO enrollMember(Long cohortId, CohortMemberEnrollRequest request) {
        requireCohort(cohortId);
        if (!enrollIfAbsent(cohortId, request.getEmpiId(), request.getGroupLabel())) {
            throw new BusinessException(ErrorCode.CONFLICT, "患者已在队列中");
        }
        refreshMemberCount(cohortId);
        ResCohortMember member = resCohortMemberMapper.selectOne(new LambdaQueryWrapper<ResCohortMember>()
                .eq(ResCohortMember::getCohortId, cohortId)
                .eq(ResCohortMember::getEmpiId, request.getEmpiId()));
        return toMemberVO(member);
    }

    public PageResult<CohortMemberVO> listMembers(Long cohortId, PageQuery query) {
        requireCohort(cohortId);
        Page<ResCohortMember> page = resCohortMemberMapper.selectPage(
                new Page<ResCohortMember>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<ResCohortMember>()
                        .eq(ResCohortMember::getCohortId, cohortId)
                        .orderByDesc(ResCohortMember::getEnrollDate));
        List<CohortMemberVO> items = new ArrayList<CohortMemberVO>();
        for (ResCohortMember member : page.getRecords()) {
            items.add(toMemberVO(member));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    @Transactional
    public void removeMember(Long cohortId, Long memberId) {
        requireCohort(cohortId);
        ResCohortMember member = resCohortMemberMapper.selectById(memberId);
        if (member == null || !member.getCohortId().equals(cohortId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "队列成员不存在");
        }
        resCohortMemberMapper.deleteById(memberId);
        refreshMemberCount(cohortId);
    }

    public ResCohort requireCohort(Long id) {
        ResCohort cohort = resCohortMapper.selectById(id);
        if (cohort == null || cohort.getDeleted() != null && cohort.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "队列不存在");
        }
        if (!cohort.getOrgId().equals(ResearchOrgContext.requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该队列");
        }
        return cohort;
    }

    @Transactional
    public int syncDynamicMembers(Long cohortId) {
        ResCohort cohort = requireCohort(cohortId);
        List<ResIdxSearchDocument> documents = resIdxSearchDocumentMapper.selectList(
                new LambdaQueryWrapper<ResIdxSearchDocument>().eq(ResIdxSearchDocument::getOrgId, cohort.getOrgId()));

        int changed = 0;
        for (ResIdxSearchDocument document : documents) {
            boolean matched = cohortRuleEngine.evaluate(cohort.getRuleJson(), document);
            ResCohortMember existing = resCohortMemberMapper.selectOne(new LambdaQueryWrapper<ResCohortMember>()
                    .eq(ResCohortMember::getCohortId, cohortId)
                    .eq(ResCohortMember::getEmpiId, document.getEmpiId()));
            if (matched && existing == null) {
                enrollIfAbsent(cohortId, document.getEmpiId(), null);
                changed++;
            } else if (!matched && existing != null && "ACTIVE".equals(existing.getStatus())) {
                existing.setStatus("WITHDRAWN");
                resCohortMemberMapper.updateById(existing);
                changed++;
            }
        }
        refreshMemberCount(cohortId);
        return changed;
    }

    private boolean enrollIfAbsent(Long cohortId, Long empiId, String groupLabel) {
        ResCohortMember existing = resCohortMemberMapper.selectOne(new LambdaQueryWrapper<ResCohortMember>()
                .eq(ResCohortMember::getCohortId, cohortId)
                .eq(ResCohortMember::getEmpiId, empiId));
        if (existing != null) {
            if ("WITHDRAWN".equals(existing.getStatus())) {
                existing.setStatus("ACTIVE");
                existing.setEnrollDate(LocalDate.now());
                if (groupLabel != null) {
                    existing.setGroupLabel(groupLabel);
                }
                resCohortMemberMapper.updateById(existing);
                return true;
            }
            return false;
        }
        ResCohortMember member = new ResCohortMember();
        member.setId(IdGenerator.nextId());
        member.setCohortId(cohortId);
        member.setEmpiId(empiId);
        member.setGroupLabel(groupLabel);
        member.setEnrollDate(LocalDate.now());
        member.setStatus("ACTIVE");
        resCohortMemberMapper.insert(member);
        return true;
    }

    private void refreshMemberCount(Long cohortId) {
        Long count = resCohortMemberMapper.selectCount(new LambdaQueryWrapper<ResCohortMember>()
                .eq(ResCohortMember::getCohortId, cohortId)
                .eq(ResCohortMember::getStatus, "ACTIVE"));
        ResCohort cohort = resCohortMapper.selectById(cohortId);
        cohort.setMemberCount(count != null ? count.intValue() : 0);
        resCohortMapper.updateById(cohort);
    }

    private CohortVO toVO(ResCohort cohort) {
        CohortVO vo = new CohortVO();
        vo.setId(cohort.getId());
        vo.setProjectId(cohort.getProjectId());
        vo.setCohortName(cohort.getCohortName());
        vo.setCohortType(cohort.getCohortType());
        vo.setRuleJson(cohort.getRuleJson());
        vo.setMemberCount(cohort.getMemberCount());
        vo.setCreatedAt(cohort.getCreatedAt());
        return vo;
    }

    private CohortMemberVO toMemberVO(ResCohortMember member) {
        CohortMemberVO vo = new CohortMemberVO();
        vo.setId(member.getId());
        vo.setCohortId(member.getCohortId());
        vo.setEmpiId(member.getEmpiId());
        vo.setGroupLabel(member.getGroupLabel());
        vo.setEnrollDate(member.getEnrollDate());
        vo.setStatus(member.getStatus());
        return vo;
    }
}

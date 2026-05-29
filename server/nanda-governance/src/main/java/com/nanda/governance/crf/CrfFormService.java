package com.nanda.governance.crf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.security.context.AuthContext;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.domain.dto.CrfFormCreateRequest;
import com.nanda.governance.domain.dto.CrfFormVO;
import com.nanda.governance.domain.dto.CrfResponseSubmitRequest;
import com.nanda.governance.domain.dto.CrfResponseVO;
import com.nanda.governance.domain.entity.GovCrfForm;
import com.nanda.governance.domain.entity.GovCrfResponse;
import com.nanda.governance.mapper.GovCrfFormMapper;
import com.nanda.governance.mapper.GovCrfResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrfFormService {

    private final GovCrfFormMapper govCrfFormMapper;
    private final GovCrfResponseMapper govCrfResponseMapper;
    private final CrfScoreEngine crfScoreEngine;

    public PageResult<CrfFormVO> listForms(PageQuery query) {
        Long orgId = requireOrgId();
        Page<GovCrfForm> page = govCrfFormMapper.selectPage(
                new Page<GovCrfForm>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<GovCrfForm>()
                        .eq(GovCrfForm::getOrgId, orgId)
                        .eq(GovCrfForm::getDeleted, 0)
                        .orderByDesc(GovCrfForm::getUpdatedAt));
        List<CrfFormVO> items = new ArrayList<CrfFormVO>();
        for (GovCrfForm form : page.getRecords()) {
            items.add(toFormVO(form));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    @Transactional
    public CrfFormVO createForm(CrfFormCreateRequest request) {
        Long orgId = requireOrgId();
        AuthContext ctx = AuthContextHolder.get();
        GovCrfForm form = new GovCrfForm();
        form.setId(IdGenerator.nextId());
        form.setFormCode(request.getFormCode());
        form.setFormName(request.getFormName());
        form.setSpecialtyType(request.getSpecialtyType());
        form.setVersion(1);
        form.setSchemaJson(request.getSchemaJson());
        form.setScoreRulesJson(request.getScoreRulesJson());
        form.setStatus("DRAFT");
        form.setOrgId(orgId);
        form.setCreatedBy(ctx != null ? ctx.getUserId() : null);
        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        govCrfFormMapper.insert(form);
        return toFormVO(form);
    }

    @Transactional
    public CrfFormVO publishForm(Long id) {
        GovCrfForm form = requireForm(id);
        form.setStatus("PUBLISHED");
        form.setPublishedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        govCrfFormMapper.updateById(form);
        return toFormVO(form);
    }

    @Transactional
    public CrfResponseVO submitResponse(CrfResponseSubmitRequest request) {
        GovCrfForm form = requireForm(request.getFormId());
        if (!"PUBLISHED".equals(form.getStatus())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE, "表单未发布");
        }
        Map<String, Object> answers = JsonUtils.fromJson(request.getAnswersJson(),
                new TypeReference<Map<String, Object>>() {
                });
        Map<String, java.math.BigDecimal> scores = crfScoreEngine.calculateScores(form.getScoreRulesJson(), answers);

        AuthContext ctx = AuthContextHolder.get();
        GovCrfResponse response = new GovCrfResponse();
        response.setId(IdGenerator.nextId());
        response.setFormId(form.getId());
        response.setFormVersion(form.getVersion());
        response.setEmpiId(request.getEmpiId());
        response.setProjectId(request.getProjectId());
        response.setAnswersJson(request.getAnswersJson());
        response.setScoresJson(JsonUtils.toJson(scores));
        response.setStatus("SUBMITTED");
        response.setSubmittedBy(ctx != null ? ctx.getUserId() : null);
        response.setOrgId(requireOrgId());
        response.setCreatedAt(LocalDateTime.now());
        govCrfResponseMapper.insert(response);
        return toResponseVO(response);
    }

    public PageResult<CrfResponseVO> listResponses(PageQuery query, Long formId) {
        LambdaQueryWrapper<GovCrfResponse> wrapper = new LambdaQueryWrapper<GovCrfResponse>()
                .eq(GovCrfResponse::getOrgId, requireOrgId())
                .eq(formId != null, GovCrfResponse::getFormId, formId)
                .orderByDesc(GovCrfResponse::getCreatedAt);
        Page<GovCrfResponse> page = govCrfResponseMapper.selectPage(
                new Page<GovCrfResponse>(query.getPage(), query.getSize()), wrapper);
        List<CrfResponseVO> items = new ArrayList<CrfResponseVO>();
        for (GovCrfResponse r : page.getRecords()) {
            items.add(toResponseVO(r));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    private GovCrfForm requireForm(Long id) {
        GovCrfForm form = govCrfFormMapper.selectById(id);
        if (form == null || form.getDeleted() != null && form.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "CRF表单不存在");
        }
        if (!form.getOrgId().equals(requireOrgId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该表单");
        }
        return form;
    }

    private Long requireOrgId() {
        AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getOrgId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "缺少机构上下文");
        }
        return ctx.getOrgId();
    }

    private CrfFormVO toFormVO(GovCrfForm form) {
        CrfFormVO vo = new CrfFormVO();
        vo.setId(form.getId());
        vo.setFormCode(form.getFormCode());
        vo.setFormName(form.getFormName());
        vo.setSpecialtyType(form.getSpecialtyType());
        vo.setVersion(form.getVersion());
        vo.setSchemaJson(form.getSchemaJson());
        vo.setScoreRulesJson(form.getScoreRulesJson());
        vo.setStatus(form.getStatus());
        vo.setPublishedAt(form.getPublishedAt());
        vo.setCreatedAt(form.getCreatedAt());
        return vo;
    }

    private CrfResponseVO toResponseVO(GovCrfResponse response) {
        CrfResponseVO vo = new CrfResponseVO();
        vo.setId(response.getId());
        vo.setFormId(response.getFormId());
        vo.setFormVersion(response.getFormVersion());
        vo.setEmpiId(response.getEmpiId());
        vo.setProjectId(response.getProjectId());
        vo.setAnswersJson(response.getAnswersJson());
        vo.setScoresJson(response.getScoresJson());
        vo.setStatus(response.getStatus());
        vo.setCreatedAt(response.getCreatedAt());
        return vo;
    }
}

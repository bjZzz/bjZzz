package com.nanda.analytics.sandbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateCreateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateUpdateRequest;
import com.nanda.analytics.domain.dto.AnalyticsW8Dtos.ScriptTemplateVO;
import com.nanda.analytics.domain.entity.AnaScriptTemplate;
import com.nanda.analytics.mapper.AnaScriptTemplateMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptTemplateService {

    private final AnaScriptTemplateMapper scriptTemplateMapper;

    public List<ScriptTemplateVO> list() {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        List<AnaScriptTemplate> rows = scriptTemplateMapper.selectList(
                new LambdaQueryWrapper<AnaScriptTemplate>()
                        .and(w -> w.eq(AnaScriptTemplate::getOrgId, orgId).or().isNull(AnaScriptTemplate::getOrgId))
                        .orderByDesc(AnaScriptTemplate::getCreatedAt));
        List<ScriptTemplateVO> result = new ArrayList<ScriptTemplateVO>();
        for (AnaScriptTemplate row : rows) {
            result.add(toVO(row));
        }
        return result;
    }

    public ScriptTemplateVO getByCode(String templateCode) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        AnaScriptTemplate entity = scriptTemplateMapper.selectOne(
                new LambdaQueryWrapper<AnaScriptTemplate>()
                        .eq(AnaScriptTemplate::getTemplateCode, templateCode)
                        .and(w -> w.eq(AnaScriptTemplate::getOrgId, orgId).or().isNull(AnaScriptTemplate::getOrgId))
                        .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "脚本模板不存在");
        }
        return toVO(entity);
    }

    @Transactional
    public ScriptTemplateVO create(ScriptTemplateCreateRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        if (!StringUtils.hasText(request.getTemplateCode())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "templateCode 不能为空");
        }
        AnaScriptTemplate entity = new AnaScriptTemplate();
        entity.setId(IdGenerator.nextId());
        entity.setTemplateCode(request.getTemplateCode());
        entity.setTemplateName(request.getTemplateName());
        entity.setScriptContent(request.getScriptContent());
        entity.setOrgId(orgId);
        entity.setCreatedAt(LocalDateTime.now());
        scriptTemplateMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public ScriptTemplateVO update(String templateCode, ScriptTemplateUpdateRequest request) {
        AnaScriptTemplate entity = requireOwnedTemplate(templateCode);
        if (StringUtils.hasText(request.getTemplateName())) {
            entity.setTemplateName(request.getTemplateName());
        }
        if (request.getScriptContent() != null) {
            entity.setScriptContent(request.getScriptContent());
        }
        scriptTemplateMapper.updateById(entity);
        return toVO(entity);
    }

    private AnaScriptTemplate requireOwnedTemplate(String templateCode) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        AnaScriptTemplate entity = scriptTemplateMapper.selectOne(
                new LambdaQueryWrapper<AnaScriptTemplate>()
                        .eq(AnaScriptTemplate::getTemplateCode, templateCode)
                        .eq(AnaScriptTemplate::getOrgId, orgId)
                        .last("LIMIT 1"));
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "脚本模板不存在或无权修改");
        }
        return entity;
    }

    private ScriptTemplateVO toVO(AnaScriptTemplate entity) {
        ScriptTemplateVO vo = new ScriptTemplateVO();
        vo.setId(entity.getId());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setScriptContent(entity.getScriptContent());
        vo.setOrgId(entity.getOrgId());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}

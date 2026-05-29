package com.nanda.asset.comorbidity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityPatientDetailVO;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityRuleCreateRequest;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityRuleVO;
import com.nanda.asset.domain.dto.AssetDtos.ComorbidityViewVO;
import com.nanda.asset.domain.dto.AssetDtos.SpecialtyPatientVO;
import com.nanda.asset.domain.entity.PubComorbidityRule;
import com.nanda.asset.domain.entity.PubComorbidityView;
import com.nanda.asset.mapper.PubComorbidityRuleMapper;
import com.nanda.asset.mapper.PubComorbidityViewMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.core.result.PageQuery;
import com.nanda.common.core.result.PageResult;
import com.nanda.common.util.IdGenerator;
import com.nanda.governance.publish.entity.EmpiMaster;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.EmpiMasterMapper;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComorbidityService {

    private final PubComorbidityRuleMapper pubComorbidityRuleMapper;
    private final PubComorbidityViewMapper pubComorbidityViewMapper;
    private final EmpiMasterMapper empiMasterMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final ComorbidityViewRefresher comorbidityViewRefresher;

    public List<ComorbidityRuleVO> listRules() {
        List<PubComorbidityRule> rules = pubComorbidityRuleMapper.selectList(new LambdaQueryWrapper<PubComorbidityRule>()
                .eq(PubComorbidityRule::getDeleted, 0)
                .and(w -> w.eq(PubComorbidityRule::getOrgId, AssetOrgContext.requireOrgId()).or().isNull(PubComorbidityRule::getOrgId)));
        List<ComorbidityRuleVO> result = new ArrayList<ComorbidityRuleVO>();
        for (PubComorbidityRule rule : rules) {
            result.add(toRuleVO(rule));
        }
        return result;
    }

    @Transactional
    public ComorbidityRuleVO createRule(ComorbidityRuleCreateRequest request) {
        PubComorbidityRule rule = new PubComorbidityRule();
        rule.setId(IdGenerator.nextId());
        rule.setRuleName(request.getRuleName());
        rule.setExpressionJson(request.getExpressionJson());
        rule.setTimeWindowJson(request.getTimeWindowJson());
        rule.setStatus("ACTIVE");
        rule.setOrgId(AssetOrgContext.requireOrgId());
        rule.setDeleted(0);
        pubComorbidityRuleMapper.insert(rule);
        return toRuleVO(rule);
    }

    public PageResult<ComorbidityViewVO> listViews(PageQuery query, Long ruleId) {
        LambdaQueryWrapper<PubComorbidityView> wrapper = new LambdaQueryWrapper<PubComorbidityView>()
                .eq(ruleId != null, PubComorbidityView::getRuleId, ruleId)
                .orderByDesc(PubComorbidityView::getRefreshedAt);
        Page<PubComorbidityView> page = pubComorbidityViewMapper.selectPage(
                new Page<PubComorbidityView>(query.getPage(), query.getSize()), wrapper);
        List<ComorbidityViewVO> items = new ArrayList<ComorbidityViewVO>();
        for (PubComorbidityView view : page.getRecords()) {
            items.add(toViewVO(view));
        }
        return PageResult.of(items, query.getPage(), query.getSize(), page.getTotal());
    }

    public ComorbidityPatientDetailVO getPatientDetail(Long viewId, Long empiId) {
        PubComorbidityView view = pubComorbidityViewMapper.selectById(viewId);
        if (view == null || !view.getEmpiId().equals(empiId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "共病视图不存在");
        }
        EmpiMaster empi = empiMasterMapper.selectById(empiId);
        List<PubSpecialtyPatient> records = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getEmpiId, empiId)
                .eq(PubSpecialtyPatient::getOrgId, AssetOrgContext.requireOrgId())
                .eq(PubSpecialtyPatient::getDeleted, 0));
        ComorbidityPatientDetailVO vo = new ComorbidityPatientDetailVO();
        vo.setEmpiId(empiId);
        vo.setDisplayName(empi != null ? empi.getDisplayName() : null);
        vo.setComorbidityLabels(view.getComorbidityLabels());
        List<SpecialtyPatientVO> specialtyRecords = new ArrayList<SpecialtyPatientVO>();
        for (PubSpecialtyPatient record : records) {
            SpecialtyPatientVO item = new SpecialtyPatientVO();
            item.setId(record.getId());
            item.setEmpiId(record.getEmpiId());
            item.setSpecialtyType(record.getSpecialtyType());
            item.setStatus(record.getStatus());
            item.setCoreFields(record.getCoreFields());
            item.setCreatedAt(record.getCreatedAt());
            specialtyRecords.add(item);
        }
        vo.setSpecialtyRecords(specialtyRecords);
        return vo;
    }

    public int refreshRule(Long ruleId) {
        return comorbidityViewRefresher.refreshRule(ruleId);
    }

    private ComorbidityRuleVO toRuleVO(PubComorbidityRule rule) {
        ComorbidityRuleVO vo = new ComorbidityRuleVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setExpressionJson(rule.getExpressionJson());
        vo.setStatus(rule.getStatus());
        return vo;
    }

    private ComorbidityViewVO toViewVO(PubComorbidityView view) {
        ComorbidityViewVO vo = new ComorbidityViewVO();
        vo.setId(view.getId());
        vo.setRuleId(view.getRuleId());
        PubComorbidityRule rule = pubComorbidityRuleMapper.selectById(view.getRuleId());
        vo.setRuleName(rule != null ? rule.getRuleName() : null);
        vo.setEmpiId(view.getEmpiId());
        EmpiMaster empi = empiMasterMapper.selectById(view.getEmpiId());
        vo.setDisplayName(empi != null ? empi.getDisplayName() : null);
        vo.setComorbidityLabels(view.getComorbidityLabels());
        vo.setRefreshedAt(view.getRefreshedAt());
        return vo;
    }
}

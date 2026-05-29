package com.nanda.asset.comorbidity;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.asset.domain.entity.PubComorbidityRule;
import com.nanda.asset.domain.entity.PubComorbidityView;
import com.nanda.asset.mapper.PubComorbidityRuleMapper;
import com.nanda.asset.mapper.PubComorbidityViewMapper;
import com.nanda.asset.service.AssetOrgContext;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import com.nanda.governance.publish.mapper.PubSpecialtyPatientMapper;
import com.nanda.common.event.ComorbidityViewRefreshEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComorbidityViewRefresher {

    private final PubComorbidityRuleMapper pubComorbidityRuleMapper;
    private final PubComorbidityViewMapper pubComorbidityViewMapper;
    private final PubSpecialtyPatientMapper pubSpecialtyPatientMapper;
    private final ComorbidityRuleEngine comorbidityRuleEngine;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void refreshForEmpi(Long empiId, Long orgId) {
        List<PubComorbidityRule> rules = pubComorbidityRuleMapper.selectList(new LambdaQueryWrapper<PubComorbidityRule>()
                .eq(PubComorbidityRule::getDeleted, 0)
                .eq(PubComorbidityRule::getStatus, "ACTIVE")
                .and(w -> w.eq(PubComorbidityRule::getOrgId, orgId).or().isNull(PubComorbidityRule::getOrgId)));

        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getEmpiId, empiId)
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0));

        List<Long> refreshedRuleIds = new ArrayList<Long>();
        for (PubComorbidityRule rule : rules) {
            ComorbidityRuleEngine.EvaluationResult result = comorbidityRuleEngine.evaluate(rule, empiId, patients);
            Long existingId = pubComorbidityViewMapper.findViewId(rule.getId(), empiId);
            if (result.isMatched()) {
                upsertView(existingId, rule.getId(), empiId, result);
                refreshedRuleIds.add(rule.getId());
            } else if (existingId != null) {
                pubComorbidityViewMapper.deleteById(existingId);
            }
        }
        if (!refreshedRuleIds.isEmpty()) {
            eventPublisher.publishEvent(new ComorbidityViewRefreshEvent(this, empiId, refreshedRuleIds, orgId));
        }
    }

    @Transactional
    public int refreshRule(Long ruleId) {
        PubComorbidityRule rule = pubComorbidityRuleMapper.selectById(ruleId);
        if (rule == null || rule.getDeleted() != null && rule.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "共病规则不存在");
        }
        Long orgId = AssetOrgContext.requireOrgId();
        List<PubSpecialtyPatient> patients = pubSpecialtyPatientMapper.selectList(new LambdaQueryWrapper<PubSpecialtyPatient>()
                .eq(PubSpecialtyPatient::getOrgId, orgId)
                .eq(PubSpecialtyPatient::getDeleted, 0));
        int refreshed = 0;
        java.util.Set<Long> empiIds = new java.util.HashSet<Long>();
        for (PubSpecialtyPatient patient : patients) {
            empiIds.add(patient.getEmpiId());
        }
        for (Long empiId : empiIds) {
            refreshForEmpi(empiId, orgId);
            refreshed++;
        }
        return refreshed;
    }

    private void upsertView(Long existingId, Long ruleId, Long empiId, ComorbidityRuleEngine.EvaluationResult result) {
        if (existingId != null) {
            PubComorbidityView view = pubComorbidityViewMapper.selectById(existingId);
            view.setSpecialtyRecordIds(JsonUtils.toJson(result.getRecordIds()));
            view.setComorbidityLabels(JsonUtils.toJson(result.getLabels()));
            view.setRefreshVersion(view.getRefreshVersion() == null ? 1 : view.getRefreshVersion() + 1);
            view.setRefreshedAt(LocalDateTime.now());
            pubComorbidityViewMapper.updateById(view);
            return;
        }
        PubComorbidityView view = new PubComorbidityView();
        view.setId(IdGenerator.nextId());
        view.setRuleId(ruleId);
        view.setEmpiId(empiId);
        view.setSpecialtyRecordIds(JsonUtils.toJson(result.getRecordIds()));
        view.setComorbidityLabels(JsonUtils.toJson(result.getLabels()));
        view.setRefreshVersion(1);
        view.setRefreshedAt(LocalDateTime.now());
        pubComorbidityViewMapper.insert(view);
    }
}

package com.nanda.governance.cleaning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.governance.domain.dto.CleaningRuleCreateRequest;
import com.nanda.governance.domain.dto.CleaningRuleVO;
import com.nanda.governance.domain.entity.GovCleaningRule;
import com.nanda.governance.mapper.GovCleaningRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleaningRuleService {

    private final GovCleaningRuleMapper govCleaningRuleMapper;

    public List<CleaningRuleVO> list() {
        Long orgId = AuthContextHolder.get().getOrgId();
        List<GovCleaningRule> rules = govCleaningRuleMapper.selectList(new LambdaQueryWrapper<GovCleaningRule>()
                .eq(GovCleaningRule::getDeleted, 0)
                .and(w -> w.eq(GovCleaningRule::getOrgId, orgId).or().isNull(GovCleaningRule::getOrgId)));
        List<CleaningRuleVO> result = new ArrayList<CleaningRuleVO>();
        for (GovCleaningRule rule : rules) {
            result.add(toVO(rule));
        }
        return result;
    }

    public CleaningRuleVO create(CleaningRuleCreateRequest request) {
        GovCleaningRule rule = new GovCleaningRule();
        rule.setId(IdGenerator.nextId());
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleType(request.getRuleType().toUpperCase());
        rule.setRuleConfigJson(request.getRuleConfigJson());
        rule.setSpecialtyType(request.getSpecialtyType());
        rule.setStatus("ACTIVE");
        rule.setOrgId(AuthContextHolder.get().getOrgId());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        govCleaningRuleMapper.insert(rule);
        return toVO(rule);
    }

    private CleaningRuleVO toVO(GovCleaningRule rule) {
        CleaningRuleVO vo = new CleaningRuleVO();
        vo.setId(rule.getId());
        vo.setRuleCode(rule.getRuleCode());
        vo.setRuleType(rule.getRuleType());
        vo.setRuleConfigJson(rule.getRuleConfigJson());
        vo.setSpecialtyType(rule.getSpecialtyType());
        vo.setStatus(rule.getStatus());
        return vo;
    }
}

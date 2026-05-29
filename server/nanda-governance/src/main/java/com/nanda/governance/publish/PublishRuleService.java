package com.nanda.governance.publish;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.common.security.context.AuthContextHolder;
import com.nanda.common.util.IdGenerator;
import com.nanda.governance.domain.dto.PublishRuleCreateRequest;
import com.nanda.governance.domain.dto.PublishRuleVO;
import com.nanda.governance.domain.entity.GovPublishRule;
import com.nanda.governance.mapper.GovPublishRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishRuleService {

    private final GovPublishRuleMapper govPublishRuleMapper;

    public List<PublishRuleVO> list() {
        Long orgId = AuthContextHolder.get().getOrgId();
        List<GovPublishRule> rules = govPublishRuleMapper.selectList(new LambdaQueryWrapper<GovPublishRule>()
                .eq(GovPublishRule::getDeleted, 0)
                .and(w -> w.eq(GovPublishRule::getOrgId, orgId).or().isNull(GovPublishRule::getOrgId)));
        List<PublishRuleVO> result = new ArrayList<PublishRuleVO>();
        for (GovPublishRule rule : rules) {
            result.add(toVO(rule));
        }
        return result;
    }

    public PublishRuleVO create(PublishRuleCreateRequest request) {
        GovPublishRule rule = new GovPublishRule();
        rule.setId(IdGenerator.nextId());
        rule.setRuleName(request.getRuleName());
        rule.setSpecialtyType(request.getSpecialtyType());
        rule.setInclusionJson(request.getInclusionJson());
        rule.setFieldMappingId(request.getFieldMappingId());
        rule.setOrgId(AuthContextHolder.get().getOrgId());
        rule.setDeleted(0);
        govPublishRuleMapper.insert(rule);
        return toVO(rule);
    }

    private PublishRuleVO toVO(GovPublishRule rule) {
        PublishRuleVO vo = new PublishRuleVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setSpecialtyType(rule.getSpecialtyType());
        vo.setInclusionJson(rule.getInclusionJson());
        vo.setFieldMappingId(rule.getFieldMappingId());
        return vo;
    }
}

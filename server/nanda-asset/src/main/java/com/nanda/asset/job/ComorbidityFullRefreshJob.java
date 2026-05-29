package com.nanda.asset.job;

import com.nanda.asset.comorbidity.ComorbidityViewRefresher;
import com.nanda.asset.domain.entity.PubComorbidityRule;
import com.nanda.asset.mapper.PubComorbidityRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComorbidityFullRefreshJob {

    private final PubComorbidityRuleMapper pubComorbidityRuleMapper;
    private final ComorbidityViewRefresher comorbidityViewRefresher;

    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshAll() {
        List<PubComorbidityRule> rules = pubComorbidityRuleMapper.selectList(new LambdaQueryWrapper<PubComorbidityRule>()
                .eq(PubComorbidityRule::getDeleted, 0)
                .eq(PubComorbidityRule::getStatus, "ACTIVE"));
        int total = 0;
        for (PubComorbidityRule rule : rules) {
            total += comorbidityViewRefresher.refreshRule(rule.getId());
        }
        log.info("comorbidityFullRefresh completed rules={} empiCount={}", rules.size(), total);
    }
}

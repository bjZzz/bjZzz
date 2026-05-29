package com.nanda.asset.listener;

import com.nanda.common.event.ComorbidityViewRefreshEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ComorbidityViewRefreshListener {

    @EventListener
    public void onRefresh(ComorbidityViewRefreshEvent event) {
        log.info("Asset comorbidity view refreshed empiId={} ruleIds={}", event.getEmpiId(), event.getRuleIds());
    }
}

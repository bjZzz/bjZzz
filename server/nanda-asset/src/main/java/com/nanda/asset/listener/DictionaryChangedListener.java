package com.nanda.asset.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nanda.common.event.DictionaryChangedEvent;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DictionaryChangedListener {

    @EventListener
    public void onDictionaryChanged(DictionaryChangedEvent event) {
        log.info("Asset cache invalidate dictType={} orgId={}", event.getDictType(), event.getOrgId());
    }
}

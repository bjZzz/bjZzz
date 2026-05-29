package com.nanda.analytics.listener;

import com.nanda.analytics.index.IndexSyncWorker;
import com.nanda.common.event.DataPublishedEvent;
import com.nanda.common.event.IndexSyncRequiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class IndexSyncLocalListener {

    private final IndexSyncWorker indexSyncWorker;

    @EventListener
    public void onDataPublished(DataPublishedEvent event) {
        if (event.getEmpiId() == null || event.getOrgId() == null) {
            return;
        }
        log.info("Analytics local DataPublished empiId={}", event.getEmpiId());
        indexSyncWorker.syncEmpi(event.getEmpiId(), event.getOrgId(), "PATIENT");
    }

    @EventListener
    public void onIndexSyncRequired(IndexSyncRequiredEvent event) {
        if (event.getEmpiId() == null || event.getOrgId() == null) {
            return;
        }
        log.info("Analytics local IndexSyncRequired empiId={}", event.getEmpiId());
        indexSyncWorker.syncEmpi(event.getEmpiId(), event.getOrgId(),
                event.getDocType() != null ? event.getDocType() : "PATIENT");
    }
}

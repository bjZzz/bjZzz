package com.nanda.asset.listener;

import com.nanda.asset.comorbidity.ComorbidityViewRefresher;
import com.nanda.common.event.IndexSyncRequiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataPublishedHandler {

    private final ComorbidityViewRefresher comorbidityViewRefresher;
    private final ApplicationEventPublisher eventPublisher;

    public void handle(Long empiId, String specialtyType, Long recordId, Long orgId) {
        if (empiId == null || orgId == null) {
            return;
        }
        log.info("Asset handle DataPublished empiId={} specialtyType={} recordId={}",
                empiId, specialtyType, recordId);
        comorbidityViewRefresher.refreshForEmpi(empiId, orgId);
        eventPublisher.publishEvent(new IndexSyncRequiredEvent(this, empiId, "PATIENT", orgId));
    }
}

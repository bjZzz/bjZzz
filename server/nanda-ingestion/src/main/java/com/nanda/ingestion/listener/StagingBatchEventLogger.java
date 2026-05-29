package com.nanda.ingestion.listener;

import com.nanda.common.event.StagingBatchReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StagingBatchEventLogger {

    @EventListener
    public void onBatchReceived(StagingBatchReceivedEvent event) {
        log.info("StagingBatchReceived batchId={} orgId={} recordCount={}",
                event.getBatchId(), event.getOrgId(), event.getRecordCount());
    }
}

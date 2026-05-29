package com.nanda.governance.listener;

import com.nanda.common.event.StagingBatchReceivedEvent;
import com.nanda.governance.publish.PublishingPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class StagingBatchListener {

    private final PublishingPipeline publishingPipeline;

    @EventListener
    public void onStagingBatchReceived(StagingBatchReceivedEvent event) {
        log.info("Governance received StagingBatchReceived batchId={}", event.getBatchId());
        publishingPipeline.processBatch(event.getBatchId());
    }
}

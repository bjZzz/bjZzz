package com.nanda.governance.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.event.DomainEventRouting;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.PublishingPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "true")
public class StagingBatchMqConsumer {

    private final PublishingPipeline publishingPipeline;

    @RabbitListener(queues = DomainEventRouting.QUEUE_GOV_STAGING)
    public void onMessage(String message) {
        Map<String, Object> payload = JsonUtils.fromJson(message, new TypeReference<Map<String, Object>>() {
        });
        Long batchId = longVal(payload.get("batchId"));
        if (batchId == null) {
            return;
        }
        log.info("Governance MQ StagingBatchReceived batchId={}", batchId);
        publishingPipeline.processBatch(batchId);
    }

    private Long longVal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

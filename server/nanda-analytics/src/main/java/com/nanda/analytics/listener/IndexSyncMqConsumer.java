package com.nanda.analytics.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.index.IndexSyncWorker;
import com.nanda.common.event.DomainEventRouting;
import com.nanda.common.util.JsonUtils;
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
public class IndexSyncMqConsumer {

    private final IndexSyncWorker indexSyncWorker;

    @RabbitListener(queues = DomainEventRouting.QUEUE_ANALYTICS_DATA_PUBLISHED)
    public void onDataPublished(String message) {
        Map<String, Object> payload = JsonUtils.fromJson(message, new TypeReference<Map<String, Object>>() {
        });
        Long empiId = longVal(payload.get("empiId"));
        Long orgId = longVal(payload.get("orgId"));
        if (empiId == null || orgId == null) {
            return;
        }
        log.info("Analytics MQ DataPublished empiId={}", empiId);
        indexSyncWorker.syncEmpi(empiId, orgId, "PATIENT");
    }

    @RabbitListener(queues = DomainEventRouting.QUEUE_ANALYTICS_INDEX_SYNC)
    public void onIndexSyncRequired(String message) {
        Map<String, Object> payload = JsonUtils.fromJson(message, new TypeReference<Map<String, Object>>() {
        });
        Long empiId = longVal(payload.get("empiId"));
        Long orgId = longVal(payload.get("orgId"));
        String docType = payload.get("docType") != null ? String.valueOf(payload.get("docType")) : "PATIENT";
        if (empiId == null || orgId == null) {
            return;
        }
        log.info("Analytics MQ IndexSyncRequired empiId={}", empiId);
        indexSyncWorker.syncEmpi(empiId, orgId, docType);
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

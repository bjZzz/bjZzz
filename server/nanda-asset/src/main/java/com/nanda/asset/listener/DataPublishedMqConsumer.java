package com.nanda.asset.listener;

import com.fasterxml.jackson.core.type.TypeReference;
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
public class DataPublishedMqConsumer {

    private final DataPublishedHandler dataPublishedHandler;

    @RabbitListener(queues = DomainEventRouting.QUEUE_ASSET_DATA_PUBLISHED)
    public void onMessage(String message) {
        Map<String, Object> payload = JsonUtils.fromJson(message, new TypeReference<Map<String, Object>>() {
        });
        dataPublishedHandler.handle(
                longVal(payload.get("empiId")),
                stringVal(payload.get("specialtyType")),
                longVal(payload.get("recordId")),
                longVal(payload.get("orgId")));
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

    private String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }
}

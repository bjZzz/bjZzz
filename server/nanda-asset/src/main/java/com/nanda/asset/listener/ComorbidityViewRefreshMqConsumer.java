package com.nanda.asset.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.event.DomainEventRouting;
import com.nanda.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "true")
public class ComorbidityViewRefreshMqConsumer {

    @RabbitListener(queues = DomainEventRouting.QUEUE_ASSET_COMORBIDITY)
    public void onMessage(String message) {
        Map<String, Object> payload = JsonUtils.fromJson(message, new TypeReference<Map<String, Object>>() {
        });
        log.info("Asset MQ comorbidity refresh empiId={} ruleIds={}", payload.get("empiId"), payload.get("ruleIds"));
    }
}

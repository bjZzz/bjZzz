package com.nanda.boot.mq;

import com.nanda.common.event.ComorbidityViewRefreshEvent;
import com.nanda.common.event.DataPublishedEvent;
import com.nanda.common.event.DictionaryChangedEvent;
import com.nanda.common.event.DomainEventRouting;
import com.nanda.common.event.IndexSyncRequiredEvent;
import com.nanda.common.event.StagingBatchReceivedEvent;
import com.nanda.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "true")
public class DomainEventMqBridge {

    private final RabbitTemplate rabbitTemplate;

    @EventListener
    public void onStagingBatchReceived(StagingBatchReceivedEvent event) {
        publish(DomainEventRouting.STAGING_RECEIVED, payload(
                "batchId", event.getBatchId(),
                "orgId", event.getOrgId(),
                "recordCount", event.getRecordCount()));
    }

    @EventListener
    public void onDataPublished(DataPublishedEvent event) {
        publish(DomainEventRouting.DATA_PUBLISHED, payload(
                "empiId", event.getEmpiId(),
                "specialtyType", event.getSpecialtyType(),
                "recordId", event.getRecordId(),
                "orgId", event.getOrgId()));
    }

    @EventListener
    public void onDictionaryChanged(DictionaryChangedEvent event) {
        publish(DomainEventRouting.DICT_CHANGED, payload(
                "dictType", event.getDictType(),
                "fieldCodes", event.getFieldCodes(),
                "orgId", event.getOrgId()));
    }

    @EventListener
    public void onComorbidityRefresh(ComorbidityViewRefreshEvent event) {
        publish(DomainEventRouting.COMORBIDITY_REFRESH, payload(
                "empiId", event.getEmpiId(),
                "ruleIds", event.getRuleIds(),
                "orgId", event.getOrgId()));
    }

    @EventListener
    public void onIndexSyncRequired(IndexSyncRequiredEvent event) {
        publish(DomainEventRouting.INDEX_SYNC, payload(
                "empiId", event.getEmpiId(),
                "docType", event.getDocType(),
                "orgId", event.getOrgId()));
    }

    private void publish(String routingKey, Map<String, Object> body) {
        String json = JsonUtils.toJson(body);
        rabbitTemplate.convertAndSend(DomainEventRouting.EXCHANGE, routingKey, json);
        log.debug("Published domain event routingKey={} body={}", routingKey, json);
    }

    private Map<String, Object> payload(Object... kv) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}

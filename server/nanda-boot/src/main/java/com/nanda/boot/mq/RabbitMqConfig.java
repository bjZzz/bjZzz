package com.nanda.boot.mq;

import com.nanda.common.event.DomainEventRouting;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "nanda.mq", name = "enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    public TopicExchange nandaDataExchange() {
        return new TopicExchange(DomainEventRouting.EXCHANGE, true, false);
    }

    @Bean
    public Queue governanceStagingQueue() {
        return new Queue(DomainEventRouting.QUEUE_GOV_STAGING, true);
    }

    @Bean
    public Queue assetDataPublishedQueue() {
        return new Queue(DomainEventRouting.QUEUE_ASSET_DATA_PUBLISHED, true);
    }

    @Bean
    public Queue analyticsDataPublishedQueue() {
        return new Queue(DomainEventRouting.QUEUE_ANALYTICS_DATA_PUBLISHED, true);
    }

    @Bean
    public Queue assetDictChangedQueue() {
        return new Queue(DomainEventRouting.QUEUE_ASSET_DICT_CHANGED, true);
    }

    @Bean
    public Queue assetComorbidityQueue() {
        return new Queue(DomainEventRouting.QUEUE_ASSET_COMORBIDITY, true);
    }

    @Bean
    public Queue analyticsIndexSyncQueue() {
        return new Queue(DomainEventRouting.QUEUE_ANALYTICS_INDEX_SYNC, true);
    }

    @Bean
    public Binding stagingReceivedBinding(Queue governanceStagingQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(governanceStagingQueue).to(nandaDataExchange).with(DomainEventRouting.STAGING_RECEIVED);
    }

    @Bean
    public Binding assetDataPublishedBinding(Queue assetDataPublishedQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(assetDataPublishedQueue).to(nandaDataExchange).with(DomainEventRouting.DATA_PUBLISHED);
    }

    @Bean
    public Binding analyticsDataPublishedBinding(Queue analyticsDataPublishedQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(analyticsDataPublishedQueue).to(nandaDataExchange).with(DomainEventRouting.DATA_PUBLISHED);
    }

    @Bean
    public Binding dictChangedBinding(Queue assetDictChangedQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(assetDictChangedQueue).to(nandaDataExchange).with(DomainEventRouting.DICT_CHANGED);
    }

    @Bean
    public Binding comorbidityRefreshBinding(Queue assetComorbidityQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(assetComorbidityQueue).to(nandaDataExchange).with(DomainEventRouting.COMORBIDITY_REFRESH);
    }

    @Bean
    public Binding indexSyncBinding(Queue analyticsIndexSyncQueue, TopicExchange nandaDataExchange) {
        return BindingBuilder.bind(analyticsIndexSyncQueue).to(nandaDataExchange).with(DomainEventRouting.INDEX_SYNC);
    }
}

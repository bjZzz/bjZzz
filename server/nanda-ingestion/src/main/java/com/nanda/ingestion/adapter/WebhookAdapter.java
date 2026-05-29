package com.nanda.ingestion.adapter;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Webhook 数据源为推送模式，数据经 {@link com.nanda.ingestion.webhook.WebhookReceiveService} 入 Staging。
 */
@Component
public class WebhookAdapter implements DataSourceAdapter {

    @Override
    public boolean supports(String protocol) {
        return "WEBHOOK".equalsIgnoreCase(protocol);
    }

    @Override
    public ConnectionTestResult testConnection(DataSourceConfig config) {
        return ConnectionTestResult.ok("Webhook 推送模式就绪，请配置订阅接收端点");
    }

    @Override
    public List<StagingRecordDTO> fetch(DataSourceConfig config, SyncCursor cursor) {
        return Collections.emptyList();
    }
}

package com.nanda.common.event;

public final class DomainEventRouting {

    public static final String EXCHANGE = "nanda.data.topic";

    public static final String STAGING_RECEIVED = "staging.received.v1";
    public static final String DATA_PUBLISHED = "data.published.v1";
    public static final String DICT_CHANGED = "dict.changed.v1";
    public static final String COMORBIDITY_REFRESH = "comorbidity.refresh.v1";
    public static final String INDEX_SYNC = "index.sync.v1";

    public static final String QUEUE_GOV_STAGING = "nanda.governance.staging-received";
    public static final String QUEUE_ASSET_DATA_PUBLISHED = "nanda.asset.data-published";
    public static final String QUEUE_ANALYTICS_DATA_PUBLISHED = "nanda.analytics.data-published";
    public static final String QUEUE_ASSET_DICT_CHANGED = "nanda.asset.dict-changed";
    public static final String QUEUE_ASSET_COMORBIDITY = "nanda.asset.comorbidity-refresh";
    public static final String QUEUE_ANALYTICS_INDEX_SYNC = "nanda.analytics.index-sync";

    private DomainEventRouting() {
    }
}

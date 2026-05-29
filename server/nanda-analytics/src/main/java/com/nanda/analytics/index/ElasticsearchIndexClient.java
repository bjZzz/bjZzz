package com.nanda.analytics.index;

import com.nanda.analytics.domain.entity.IdxSearchDocument;

public interface ElasticsearchIndexClient {

    void indexDocument(Long orgId, IdxSearchDocument document);
}

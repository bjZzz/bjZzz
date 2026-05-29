package com.nanda.analytics.index;

import com.nanda.analytics.domain.entity.IdxSearchDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "nanda.elasticsearch", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpElasticsearchIndexClient implements ElasticsearchIndexClient {

    @Override
    public void indexDocument(Long orgId, IdxSearchDocument document) {
        log.debug("Elasticsearch disabled, skip index for empiId={}", document.getEmpiId());
    }
}

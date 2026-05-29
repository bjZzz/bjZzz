package com.nanda.analytics.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.elasticsearch", name = "enabled", havingValue = "true")
public class RestElasticsearchIndexClient implements ElasticsearchIndexClient {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public void indexDocument(Long orgId, IdxSearchDocument document) {
        try {
            String indexName = indexName(orgId);
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("empiId", document.getEmpiId());
            source.put("orgId", document.getOrgId());
            source.put("specialtyTypes", JsonUtils.fromJson(document.getSpecialtyTypes(), new TypeReference<Object>() {
            }));
            source.put("diagnosisCodes", JsonUtils.fromJson(document.getDiagnosisCodes(), new TypeReference<Object>() {
            }));
            source.put("labValues", JsonUtils.fromJson(document.getLabValues(), new TypeReference<Object>() {
            }));
            source.put("demographics", JsonUtils.fromJson(document.getDemographics(), new TypeReference<Object>() {
            }));
            source.put("completenessScore", document.getCompletenessScore());
            source.put("updatedAt", document.getUpdatedAt());

            IndexRequest request = new IndexRequest(indexName)
                    .id(String.valueOf(document.getId()))
                    .source(JsonUtils.toJson(source), XContentType.JSON);
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception ex) {
            log.error("Failed to index document id={} orgId={}", document.getId(), orgId, ex);
        }
    }

    private String indexName(Long orgId) {
        return "nanda-search-" + orgId;
    }
}

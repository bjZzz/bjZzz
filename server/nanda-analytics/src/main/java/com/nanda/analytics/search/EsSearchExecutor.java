package com.nanda.analytics.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchResultVO;
import com.nanda.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "nanda.elasticsearch", name = "enabled", havingValue = "true")
public class EsSearchExecutor implements SearchExecutor {

    private final RestHighLevelClient restHighLevelClient;
    private final MySqlSearchExecutor mySqlSearchExecutor;

    @Override
    public SearchResultVO execute(ParsedSearchQuery query, Long orgId, int page, int size) {
        try {
            SearchResponse response = search(query, orgId, page, size);
            List<SearchHitVO> items = mapHits(response);
            SearchResultVO result = new SearchResultVO();
            result.setItems(items);
            result.setTotal(response.getHits().getTotalHits().value);
            result.setPage(page);
            result.setSize(size);
            return result;
        } catch (Exception ex) {
            log.warn("ES search failed, fallback to MySQL orgId={}", orgId, ex);
            return mySqlSearchExecutor.execute(query, orgId, page, size);
        }
    }

    @Override
    public long count(ParsedSearchQuery query, Long orgId) {
        try {
            SearchResponse response = search(query, orgId, 1, 0);
            return response.getHits().getTotalHits().value;
        } catch (Exception ex) {
            log.warn("ES count failed, fallback to MySQL orgId={}", orgId, ex);
            return mySqlSearchExecutor.count(query, orgId);
        }
    }

    @Override
    public List<SearchHitVO> listAll(ParsedSearchQuery query, Long orgId) {
        try {
            SearchResponse response = search(query, orgId, 1, 10000);
            return mapHits(response);
        } catch (Exception ex) {
            log.warn("ES listAll failed, fallback to MySQL orgId={}", orgId, ex);
            return mySqlSearchExecutor.listAll(query, orgId);
        }
    }

    private SearchResponse search(ParsedSearchQuery query, Long orgId, int page, int size) throws Exception {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        bool.filter(QueryBuilders.termQuery("orgId", orgId));
        if (query.getConditions() != null) {
            for (Map<String, Object> condition : query.getConditions()) {
                appendCondition(bool, query.getOperator(), condition);
            }
        }
        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .from(Math.max(0, (page - 1) * size))
                .size(size <= 0 ? 0 : size);
        SearchRequest request = new SearchRequest(indexName(orgId)).source(source);
        return restHighLevelClient.search(request, RequestOptions.DEFAULT);
    }

    private void appendCondition(BoolQueryBuilder bool, String operator, Map<String, Object> condition) {
        String field = condition.get("field") != null ? String.valueOf(condition.get("field")) : null;
        String op = condition.get("op") != null ? String.valueOf(condition.get("op")) : "eq";
        Object value = condition.get("value");
        if (field == null) {
            return;
        }
        String esField = mapField(field);
        if ("OR".equalsIgnoreCase(operator)) {
            bool.should(buildClause(esField, op, value));
        } else {
            bool.must(buildClause(esField, op, value));
        }
    }

    private org.elasticsearch.index.query.QueryBuilder buildClause(String field, String op, Object value) {
        if ("gte".equals(op)) {
            return QueryBuilders.rangeQuery(field).gte(value);
        }
        if ("lte".equals(op)) {
            return QueryBuilders.rangeQuery(field).lte(value);
        }
        if ("between".equals(op) && value instanceof List && ((List<?>) value).size() >= 2) {
            List<?> range = (List<?>) value;
            return QueryBuilders.rangeQuery(field).gte(range.get(0)).lte(range.get(1));
        }
        return QueryBuilders.matchQuery(field, value);
    }

    private String mapField(String field) {
        if ("diagnosis_code".equals(field)) {
            return "diagnosisCodes";
        }
        if ("specialty_type".equals(field)) {
            return "specialtyTypes";
        }
        if ("completeness_score".equals(field)) {
            return "completenessScore";
        }
        if ("age".equals(field)) {
            return "demographics.age";
        }
        return field;
    }

    private List<SearchHitVO> mapHits(SearchResponse response) {
        List<SearchHitVO> hits = new ArrayList<SearchHitVO>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            SearchHitVO vo = new SearchHitVO();
            Object empiId = source.get("empiId");
            vo.setEmpiId(empiId != null ? Long.valueOf(String.valueOf(empiId)) : null);
            vo.setSpecialtyTypes(JsonUtils.toJson(source.get("specialtyTypes")));
            vo.setDiagnosisCodes(JsonUtils.toJson(source.get("diagnosisCodes")));
            vo.setDemographics(JsonUtils.toJson(source.get("demographics")));
            Object score = source.get("completenessScore");
            vo.setCompletenessScore(score != null ? String.valueOf(score) : null);
            hits.add(vo);
        }
        return hits;
    }

    private String indexName(Long orgId) {
        return "nanda-search-" + orgId;
    }
}

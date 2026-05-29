package com.nanda.analytics.search;

import com.nanda.analytics.domain.dto.AnalyticsDtos.CountNodeVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchExecuteRequest;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchResultVO;
import com.nanda.analytics.domain.entity.AnaSearchQuery;
import com.nanda.analytics.mapper.AnaSearchQueryMapper;
import com.nanda.analytics.service.AnalyticsOrgContext;
import com.nanda.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchQueryParser searchQueryParser;
    private final MySqlSearchExecutor mySqlSearchExecutor;
    private final ObjectProvider<EsSearchExecutor> esSearchExecutorProvider;
    private final AnaSearchQueryMapper anaSearchQueryMapper;

    @Value("${nanda.elasticsearch.enabled:false}")
    private boolean esEnabled;

    @Transactional
    public SearchResultVO execute(SearchExecuteRequest request) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        ParsedSearchQuery parsed = searchQueryParser.parse(request.getQueryJson());
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;

        persistQueryIfNamed(request, orgId);
        return resolveExecutor().execute(parsed, orgId, page, size);
    }

    public List<CountNodeVO> countNodes(String queryJson) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        ParsedSearchQuery parsed = searchQueryParser.parse(queryJson);
        List<CountNodeVO> nodes = new ArrayList<CountNodeVO>();
        if (parsed.getConditions() == null) {
            return nodes;
        }
        for (Map<String, Object> condition : parsed.getConditions()) {
            CountNodeVO node = new CountNodeVO();
            node.setField(condition.get("field") != null ? String.valueOf(condition.get("field")) : null);
            node.setValue(condition.get("value") != null ? String.valueOf(condition.get("value")) : null);

            ParsedSearchQuery single = new ParsedSearchQuery();
            single.setOperator("AND");
            List<Map<String, Object>> one = new ArrayList<Map<String, Object>>();
            one.add(condition);
            single.setConditions(one);
            node.setCount(resolveExecutor().count(single, orgId));
            nodes.add(node);
        }
        return nodes;
    }

    public List<String> suggest(String prefix) {
        List<String> suggestions = new ArrayList<String>();
        if (prefix == null || prefix.isEmpty()) {
            return suggestions;
        }
        suggestions.add("diagnosis_code:" + prefix);
        suggestions.add("specialty_type:" + prefix);
        suggestions.add("hba1c");
        return suggestions;
    }

    public ParsedSearchQuery parseQuery(String queryJson) {
        return searchQueryParser.parse(queryJson);
    }

    public List<Long> resolveEmpiIds(String queryJson) {
        List<Long> empiIds = new ArrayList<Long>();
        for (com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO hit : resolveExecutorHits(queryJson)) {
            if (hit.getEmpiId() != null) {
                empiIds.add(hit.getEmpiId());
            }
        }
        return empiIds;
    }

    public List<com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO> resolveExecutorHits(String queryJson) {
        Long orgId = AnalyticsOrgContext.requireOrgId();
        ParsedSearchQuery parsed = searchQueryParser.parse(queryJson);
        return resolveExecutor().listAll(parsed, orgId);
    }

    private void persistQueryIfNamed(SearchExecuteRequest request, Long orgId) {
        if (request.getQueryName() == null || request.getQueryName().isEmpty()) {
            return;
        }
        AnaSearchQuery query = new AnaSearchQuery();
        query.setId(IdGenerator.nextId());
        query.setQueryName(request.getQueryName());
        query.setQueryJson(request.getQueryJson());
        query.setUserId(AnalyticsOrgContext.currentUserId());
        query.setOrgId(orgId);
        query.setCreatedAt(LocalDateTime.now());
        anaSearchQueryMapper.insert(query);
    }

    private SearchExecutor resolveExecutor() {
        if (esEnabled) {
            EsSearchExecutor executor = esSearchExecutorProvider.getIfAvailable();
            if (executor != null) {
                return executor;
            }
        }
        return mySqlSearchExecutor;
    }
}

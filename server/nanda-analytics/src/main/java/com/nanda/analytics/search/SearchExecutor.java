package com.nanda.analytics.search;

import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchResultVO;

import java.util.List;

public interface SearchExecutor {

    SearchResultVO execute(ParsedSearchQuery query, Long orgId, int page, int size);

    long count(ParsedSearchQuery query, Long orgId);

    List<SearchHitVO> listAll(ParsedSearchQuery query, Long orgId);
}

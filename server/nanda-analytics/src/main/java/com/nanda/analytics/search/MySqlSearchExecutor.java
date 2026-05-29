package com.nanda.analytics.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchHitVO;
import com.nanda.analytics.domain.dto.AnalyticsDtos.SearchResultVO;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.analytics.mapper.IdxSearchDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Primary
@Component
@RequiredArgsConstructor
public class MySqlSearchExecutor implements SearchExecutor {

    private final IdxSearchDocumentMapper idxSearchDocumentMapper;
    private final SearchConditionEvaluator searchConditionEvaluator;

    @Override
    public SearchResultVO execute(ParsedSearchQuery query, Long orgId, int page, int size) {
        List<SearchHitVO> matched = listAll(query, orgId);
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(matched.size(), from + size);
        List<SearchHitVO> pageItems = from >= matched.size()
                ? new ArrayList<SearchHitVO>()
                : matched.subList(from, to);

        SearchResultVO result = new SearchResultVO();
        result.setItems(pageItems);
        result.setTotal(matched.size());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Override
    public long count(ParsedSearchQuery query, Long orgId) {
        return listAll(query, orgId).size();
    }

    @Override
    public List<SearchHitVO> listAll(ParsedSearchQuery query, Long orgId) {
        List<IdxSearchDocument> documents = idxSearchDocumentMapper.selectList(
                new LambdaQueryWrapper<IdxSearchDocument>().eq(IdxSearchDocument::getOrgId, orgId));
        List<SearchHitVO> hits = new ArrayList<SearchHitVO>();
        for (IdxSearchDocument document : documents) {
            if (searchConditionEvaluator.evaluate(query, document)) {
                hits.add(toHit(document));
            }
        }
        return hits;
    }

    private SearchHitVO toHit(IdxSearchDocument document) {
        SearchHitVO hit = new SearchHitVO();
        hit.setEmpiId(document.getEmpiId());
        hit.setSpecialtyTypes(document.getSpecialtyTypes());
        hit.setDiagnosisCodes(document.getDiagnosisCodes());
        hit.setDemographics(document.getDemographics());
        hit.setCompletenessScore(document.getCompletenessScore() != null
                ? document.getCompletenessScore().toPlainString() : null);
        return hit;
    }
}

package com.nanda.analytics.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SearchQueryParser {

    public ParsedSearchQuery parse(String queryJson) {
        if (queryJson == null || queryJson.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "检索条件不能为空");
        }
        Map<String, Object> root = JsonUtils.fromJson(queryJson, new TypeReference<Map<String, Object>>() {
        });
        if (root == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "检索条件格式错误");
        }
        ParsedSearchQuery parsed = new ParsedSearchQuery();
        parsed.setOperator(root.get("operator") != null ? String.valueOf(root.get("operator")) : "AND");
        parsed.setConditions(extractConditions(root));
        return parsed;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractConditions(Map<String, Object> root) {
        Object conditionsObj = root.get("conditions");
        if (conditionsObj == null) {
            conditionsObj = root.get("rules");
        }
        if (!(conditionsObj instanceof List)) {
            return new ArrayList<Map<String, Object>>();
        }
        List<Map<String, Object>> conditions = new ArrayList<Map<String, Object>>();
        for (Object item : (List<Object>) conditionsObj) {
            if (item instanceof Map) {
                conditions.add((Map<String, Object>) item);
            }
        }
        return conditions;
    }
}

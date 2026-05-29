package com.nanda.analytics.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.analytics.domain.dto.AnalyticsDtos.ParsedSearchQuery;
import com.nanda.analytics.domain.entity.IdxSearchDocument;
import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Component
public class SearchConditionEvaluator {

    public boolean evaluate(ParsedSearchQuery query, IdxSearchDocument document) {
        List<Map<String, Object>> conditions = query.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        if ("OR".equalsIgnoreCase(query.getOperator())) {
            for (Map<String, Object> condition : conditions) {
                if (evalCondition(condition, document)) {
                    return true;
                }
            }
            return false;
        }
        for (Map<String, Object> condition : conditions) {
            if (!evalCondition(condition, document)) {
                return false;
            }
        }
        return true;
    }

    private boolean evalCondition(Map<String, Object> condition, IdxSearchDocument document) {
        String field = condition.get("field") != null ? String.valueOf(condition.get("field")) : null;
        String op = condition.get("op") != null ? String.valueOf(condition.get("op")) : "eq";
        Object value = condition.get("value");
        if (field == null) {
            return true;
        }
        Object fieldValue = resolveField(field, document);
        return compare(fieldValue, op, value);
    }

    private Object resolveField(String field, IdxSearchDocument document) {
        if ("diagnosis_code".equals(field)) {
            return document.getDiagnosisCodes();
        }
        if ("specialty_type".equals(field)) {
            return document.getSpecialtyTypes();
        }
        if ("completeness_score".equals(field)) {
            return document.getCompletenessScore();
        }
        if ("demographics".equals(field)) {
            return document.getDemographics();
        }
        if ("age".equals(field)) {
            return resolveAge(document.getDemographics());
        }
        return resolveLabValue(field, document.getLabValues());
    }

    private Integer resolveAge(String demographicsJson) {
        Map<String, Object> demographics = JsonUtils.fromJson(demographicsJson, new TypeReference<Map<String, Object>>() {
        });
        if (demographics == null || demographics.get("birthDate") == null) {
            return null;
        }
        LocalDate birthDate = LocalDate.parse(String.valueOf(demographics.get("birthDate")));
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @SuppressWarnings("unchecked")
    private Object resolveLabValue(String field, String labValuesJson) {
        List<Map<String, Object>> labs = JsonUtils.fromJson(labValuesJson, new TypeReference<List<Map<String, Object>>>() {
        });
        if (labs == null) {
            return null;
        }
        for (Map<String, Object> lab : labs) {
            Object code = lab.get("examCode");
            if (code != null && field.equalsIgnoreCase(String.valueOf(code))) {
                return lab.get("examValue");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean compare(Object fieldValue, String op, Object expected) {
        if ("in".equals(op) && expected instanceof List) {
            String text = fieldValue != null ? String.valueOf(fieldValue) : "";
            for (Object item : (List<Object>) expected) {
                if (text.contains(String.valueOf(item))) {
                    return true;
                }
            }
            return false;
        }
        if ("between".equals(op) && expected instanceof List) {
            List<Object> range = (List<Object>) expected;
            if (range.size() < 2 || fieldValue == null) {
                return false;
            }
            double val = Double.parseDouble(String.valueOf(fieldValue));
            double min = Double.parseDouble(String.valueOf(range.get(0)));
            double max = Double.parseDouble(String.valueOf(range.get(1)));
            return val >= min && val <= max;
        }
        if ("gte".equals(op)) {
            return fieldValue != null && Double.parseDouble(String.valueOf(fieldValue))
                    >= Double.parseDouble(String.valueOf(expected));
        }
        if ("lte".equals(op)) {
            return fieldValue != null && Double.parseDouble(String.valueOf(fieldValue))
                    <= Double.parseDouble(String.valueOf(expected));
        }
        if ("eq".equals(op)) {
            if (fieldValue == null) {
                return expected == null;
            }
            return String.valueOf(fieldValue).contains(String.valueOf(expected));
        }
        if (fieldValue == null) {
            return expected == null;
        }
        return String.valueOf(fieldValue).contains(String.valueOf(expected));
    }
}

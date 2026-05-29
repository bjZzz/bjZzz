package com.nanda.research.cohort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.JsonUtils;
import com.nanda.research.domain.entity.ResIdxSearchDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CohortRuleEngine {

    public boolean evaluate(String ruleJson, ResIdxSearchDocument document) {
        if (ruleJson == null || ruleJson.isEmpty()) {
            return true;
        }
        Map<String, Object> rule = JsonUtils.fromJson(ruleJson, new TypeReference<Map<String, Object>>() {
        });
        if (rule == null) {
            return true;
        }
        return evalGroup(rule, document);
    }

    @SuppressWarnings("unchecked")
    private boolean evalGroup(Map<String, Object> group, ResIdxSearchDocument document) {
        String operator = group.get("operator") != null ? String.valueOf(group.get("operator")) : "AND";
        Object rulesObj = group.get("rules");
        if (!(rulesObj instanceof List)) {
            return true;
        }
        List<Object> rules = (List<Object>) rulesObj;
        if (rules.isEmpty()) {
            return true;
        }
        if ("OR".equalsIgnoreCase(operator)) {
            for (Object rule : rules) {
                if (rule instanceof Map && evalCondition((Map<String, Object>) rule, document)) {
                    return true;
                }
            }
            return false;
        }
        for (Object rule : rules) {
            if (rule instanceof Map && !evalCondition((Map<String, Object>) rule, document)) {
                return false;
            }
        }
        return true;
    }

    private boolean evalCondition(Map<String, Object> condition, ResIdxSearchDocument document) {
        String field = condition.get("field") != null ? String.valueOf(condition.get("field")) : null;
        String op = condition.get("op") != null ? String.valueOf(condition.get("op")) : "eq";
        Object value = condition.get("value");
        if (field == null) {
            return true;
        }
        Object fieldValue = resolveField(field, document);
        return compare(fieldValue, op, value);
    }

    private Object resolveField(String field, ResIdxSearchDocument document) {
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
        if (fieldValue == null) {
            return expected == null;
        }
        return String.valueOf(fieldValue).contains(String.valueOf(expected));
    }
}

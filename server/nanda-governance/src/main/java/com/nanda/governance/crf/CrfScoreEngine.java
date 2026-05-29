package com.nanda.governance.crf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrfScoreEngine {

    public Map<String, BigDecimal> calculateScores(String scoreRulesJson, Map<String, Object> answers) {
        Map<String, BigDecimal> scores = new HashMap<String, BigDecimal>();
        if (scoreRulesJson == null || scoreRulesJson.isEmpty()) {
            return scores;
        }
        List<Map<String, Object>> rules = JsonUtils.fromJson(scoreRulesJson, new TypeReference<List<Map<String, Object>>>() {
        });
        for (Map<String, Object> rule : rules) {
            String scale = String.valueOf(rule.get("scale"));
            String formula = rule.get("formula") != null ? String.valueOf(rule.get("formula")) : "sum";
            List<String> items = JsonUtils.fromJson(JsonUtils.toJson(rule.get("items")),
                    new TypeReference<List<String>>() {
                    });
            BigDecimal total = BigDecimal.ZERO;
            if (items != null) {
                for (String item : items) {
                    Object val = answers.get(item);
                    if (val instanceof Number) {
                        total = total.add(new BigDecimal(String.valueOf(val)));
                    } else if (val != null) {
                        try {
                            total = total.add(new BigDecimal(String.valueOf(val)));
                        } catch (NumberFormatException ignored) {
                            // skip non-numeric
                        }
                    }
                }
            }
            if ("sum".equalsIgnoreCase(formula)) {
                scores.put(scale, total);
            } else {
                scores.put(scale, total);
            }
        }
        return scores;
    }
}

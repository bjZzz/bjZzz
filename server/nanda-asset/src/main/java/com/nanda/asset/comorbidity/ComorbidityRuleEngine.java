package com.nanda.asset.comorbidity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.asset.domain.entity.PubComorbidityRule;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.publish.entity.PubSpecialtyPatient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ComorbidityRuleEngine {

    public EvaluationResult evaluate(PubComorbidityRule rule, Long empiId, List<PubSpecialtyPatient> patients) {
        Map<String, Object> expression = JsonUtils.fromJson(rule.getExpressionJson(),
                new TypeReference<Map<String, Object>>() {
                });
        if (expression == null) {
            return EvaluationResult.notMatched();
        }

        Object specialtiesObj = expression.get("specialties");
        int minCount = intVal(expression.get("minCount"), 2);
        if (!(specialtiesObj instanceof List)) {
            return EvaluationResult.notMatched();
        }

        @SuppressWarnings("unchecked")
        List<String> requiredSpecialties = (List<String>) specialtiesObj;
        Set<String> matchedTypes = new HashSet<String>();
        List<Long> recordIds = new ArrayList<Long>();
        for (PubSpecialtyPatient patient : patients) {
            if (!empiId.equals(patient.getEmpiId())) {
                continue;
            }
            if (requiredSpecialties.contains(patient.getSpecialtyType())) {
                matchedTypes.add(patient.getSpecialtyType());
                recordIds.add(patient.getId());
            }
        }

        if (matchedTypes.size() < minCount) {
            return EvaluationResult.notMatched();
        }

        Object labelsObj = expression.get("labels");
        List<String> labels = new ArrayList<String>();
        if (labelsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> rawLabels = (List<Object>) labelsObj;
            for (Object label : rawLabels) {
                labels.add(String.valueOf(label));
            }
        } else {
            labels.add(rule.getRuleName());
        }

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("matchedSpecialties", new ArrayList<String>(matchedTypes));
        return EvaluationResult.matched(recordIds, labels, payload);
    }

    private int intVal(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static class EvaluationResult {
        private final boolean matched;
        private final List<Long> recordIds;
        private final List<String> labels;
        private final Map<String, Object> details;

        private EvaluationResult(boolean matched, List<Long> recordIds, List<String> labels, Map<String, Object> details) {
            this.matched = matched;
            this.recordIds = recordIds;
            this.labels = labels;
            this.details = details;
        }

        public static EvaluationResult matched(List<Long> recordIds, List<String> labels, Map<String, Object> details) {
            return new EvaluationResult(true, recordIds, labels, details);
        }

        public static EvaluationResult notMatched() {
            return new EvaluationResult(false, new ArrayList<Long>(), new ArrayList<String>(), new HashMap<String, Object>());
        }

        public boolean isMatched() {
            return matched;
        }

        public List<Long> getRecordIds() {
            return recordIds;
        }

        public List<String> getLabels() {
            return labels;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }
}

package com.nanda.governance.cleaning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.IdGenerator;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.domain.entity.GovCleaningExecution;
import com.nanda.governance.domain.entity.GovCleaningRule;
import com.nanda.governance.mapper.GovCleaningExecutionMapper;
import com.nanda.governance.mapper.GovCleaningRuleMapper;
import com.nanda.ingestion.domain.entity.StgRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CleaningEngine {

    private final GovCleaningRuleMapper govCleaningRuleMapper;
    private final GovCleaningExecutionMapper govCleaningExecutionMapper;

    public List<CleanedRecord> clean(Long batchId, Long orgId, List<StgRecord> records) {
        List<GovCleaningRule> rules = govCleaningRuleMapper.selectList(new LambdaQueryWrapper<GovCleaningRule>()
                .eq(GovCleaningRule::getStatus, "ACTIVE")
                .eq(GovCleaningRule::getDeleted, 0)
                .and(w -> w.eq(GovCleaningRule::getOrgId, orgId).or().isNull(GovCleaningRule::getOrgId)));
        List<CleanedRecord> cleaned = new ArrayList<CleanedRecord>();
        Set<String> dedupKeys = new HashSet<String>();
        for (StgRecord record : records) {
            if (!"OK".equals(record.getParseStatus())) {
                continue;
            }
            Map<String, Object> payload = parsePayload(record.getRawPayload());
            for (GovCleaningRule rule : rules) {
                payload = applyRule(rule, payload, dedupKeys);
                trace(batchId, orgId, rule.getId(), "OK", payload);
            }
            CleanedRecord cr = new CleanedRecord();
            cr.setRecordId(record.getId());
            cr.setSourceRef(record.getSourceRef());
            cr.setDomain(record.getDomain());
            cr.setPayload(payload);
            cleaned.add(cr);
        }
        return cleaned;
    }

    private Map<String, Object> applyRule(GovCleaningRule rule, Map<String, Object> payload, Set<String> dedupKeys) {
        Map<String, Object> config = parseConfig(rule.getRuleConfigJson());
        String type = rule.getRuleType();
        if ("MISSING".equals(type)) {
            return applyMissing(payload, config);
        }
        if ("ABNORMAL".equals(type)) {
            return applyAbnormal(payload, config);
        }
        if ("DEDUP".equals(type)) {
            return applyDedup(payload, config, dedupKeys);
        }
        if ("UNIT".equals(type)) {
            return applyUnit(payload, config);
        }
        return payload;
    }

    private Map<String, Object> applyMissing(Map<String, Object> payload, Map<String, Object> config) {
        String field = stringVal(config.get("field"));
        String defaultValue = stringVal(config.get("defaultValue"));
        if (field != null && (payload.get(field) == null || "".equals(String.valueOf(payload.get(field))))) {
            if (defaultValue != null) {
                payload.put(field, defaultValue);
            }
            payload.put("_missing_" + field, true);
        }
        return payload;
    }

    private Map<String, Object> applyAbnormal(Map<String, Object> payload, Map<String, Object> config) {
        String field = stringVal(config.get("field"));
        Double max = doubleVal(config.get("max"));
        Double min = doubleVal(config.get("min"));
        if (field == null || !payload.containsKey(field)) {
            return payload;
        }
        Double val = doubleVal(payload.get(field));
        if (val != null) {
            if (max != null && val > max) {
                payload.put("_abnormal_" + field, "HIGH");
            }
            if (min != null && val < min) {
                payload.put("_abnormal_" + field, "LOW");
            }
        }
        return payload;
    }

    private Map<String, Object> applyDedup(Map<String, Object> payload, Map<String, Object> config, Set<String> dedupKeys) {
        String keyField = stringVal(config.get("mergeKey"));
        if (keyField == null) {
            keyField = "id";
        }
        String key = String.valueOf(payload.get(keyField));
        if (dedupKeys.contains(key)) {
            payload.put("_dedup_skipped", true);
        } else {
            dedupKeys.add(key);
        }
        return payload;
    }

    private Map<String, Object> applyUnit(Map<String, Object> payload, Map<String, Object> config) {
        String field = stringVal(config.get("field"));
        String fromUnit = stringVal(config.get("fromUnit"));
        String toUnit = stringVal(config.get("toUnit"));
        Double factor = doubleVal(config.get("factor"));
        if (field == null || factor == null || !payload.containsKey(field)) {
            return payload;
        }
        Double val = doubleVal(payload.get(field));
        if (val != null) {
            payload.put(field, val * factor);
            payload.put(field + "_unit", toUnit != null ? toUnit : fromUnit);
        }
        return payload;
    }

    private void trace(Long batchId, Long orgId, Long ruleId, String status, Map<String, Object> result) {
        GovCleaningExecution exec = new GovCleaningExecution();
        exec.setId(IdGenerator.nextId());
        exec.setBatchId(batchId);
        exec.setRuleId(ruleId);
        exec.setStatus(status);
        exec.setResultJson(JsonUtils.toJson(result));
        exec.setOrgId(orgId);
        exec.setCreatedAt(LocalDateTime.now());
        govCleaningExecutionMapper.insert(exec);
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<String, Object>();
        }
        return JsonUtils.fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    private Map<String, Object> parseConfig(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<String, Object>();
        }
        return JsonUtils.fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    private String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Double doubleVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

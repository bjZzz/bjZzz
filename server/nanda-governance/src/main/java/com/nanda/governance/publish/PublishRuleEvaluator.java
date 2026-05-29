package com.nanda.governance.publish;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nanda.common.util.JsonUtils;
import com.nanda.governance.cleaning.CleanedRecord;
import com.nanda.governance.domain.entity.GovPublishRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PublishRuleEvaluator {

    public boolean evaluate(GovPublishRule rule, CleanedRecord record) {
        if (record.isSkipped() || record.getEmpiId() == null) {
            return false;
        }
        Map<String, Object> inclusion = JsonUtils.fromJson(rule.getInclusionJson(),
                new TypeReference<Map<String, Object>>() {
                });
        if (inclusion == null || inclusion.isEmpty()) {
            return true;
        }
        Object domainEquals = inclusion.get("domainEquals");
        if (domainEquals != null && !domainEquals.equals(record.getDomain())) {
            return false;
        }
        Object requiredField = inclusion.get("requiredField");
        if (requiredField != null) {
            Object val = record.getPayload().get(String.valueOf(requiredField));
            if (val == null || String.valueOf(val).isEmpty()) {
                return false;
            }
        }
        Object excludeAbnormal = inclusion.get("excludeAbnormal");
        if (Boolean.TRUE.equals(excludeAbnormal)) {
            for (String key : record.getPayload().keySet()) {
                if (key.startsWith("_abnormal_")) {
                    return false;
                }
            }
        }
        return true;
    }
}

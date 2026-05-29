package com.nanda.analytics.risk;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class RiskResult {

    private double score;
    private String riskLevel;
    private final Map<String, Object> detail = new LinkedHashMap<String, Object>();

    public RiskResult put(String key, Object value) {
        detail.put(key, value);
        return this;
    }
}

package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * COPD Assessment Test（CAT），8 项 0-5 分，合计 0-40。
 */
@Component
public class CatCalculator implements RiskCalculator {

    private static final String[] ITEMS = {
            "cough", "phlegm", "chestTightness", "breathlessness",
            "activityLimitation", "confidenceLeavingHome", "sleep", "energy"
    };

    @Override
    public String code() {
        return "cat";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        int total = 0;
        for (String item : ITEMS) {
            double value = optNumber(input, item, 0);
            total += Math.max(0, Math.min(5, (int) Math.round(value)));
        }

        String level;
        if (total >= 31) {
            level = "VERY_HIGH";
        } else if (total >= 21) {
            level = "HIGH";
        } else if (total >= 10) {
            level = "MODERATE";
        } else {
            level = "LOW";
        }

        RiskResult result = new RiskResult();
        result.setScore(total);
        result.setRiskLevel(level);
        result.put("impactLevel", level);
        return result;
    }
}

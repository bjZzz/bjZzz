package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CHA2DS2-VASc 房颤卒中风险评分。
 */
@Component
public class ChadsVascCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "chads2-vasc";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        double age = optNumber(input, "age", 0);
        boolean female = "F".equalsIgnoreCase(optString(input, "gender", ""))
                || "FEMALE".equalsIgnoreCase(optString(input, "gender", ""));

        int score = 0;
        score += optBool(input, "congestiveHeartFailure") ? 1 : 0;
        score += optBool(input, "hypertension") ? 1 : 0;
        score += age >= 75 ? 2 : (age >= 65 ? 1 : 0);
        score += optBool(input, "diabetes") ? 1 : 0;
        score += optBool(input, "stroke") ? 2 : 0;
        score += optBool(input, "vascularDisease") ? 1 : 0;
        score += female ? 1 : 0;

        double[] annualStroke = {0.2, 0.6, 2.2, 3.2, 4.8, 7.2, 9.7, 11.2, 10.8, 12.2};
        double risk = annualStroke[Math.min(score, annualStroke.length - 1)];

        RiskResult result = new RiskResult();
        result.setScore(score);
        result.setRiskLevel(score >= 2 ? "HIGH" : score == 1 ? "MODERATE" : "LOW");
        result.put("annualStrokeRiskPercent", risk);
        result.put("anticoagulationRecommended", score >= 2);
        return result;
    }
}

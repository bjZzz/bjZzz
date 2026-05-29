package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Elixhauser 共病指数（van Walraven 加权评分）。
 */
@Component
public class ElixhauserCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "elixhauser";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        int score = 0;
        score += optBool(input, "congestiveHeartFailure") ? 7 : 0;
        score += optBool(input, "cardiacArrhythmias") ? 5 : 0;
        score += optBool(input, "valvularDisease") ? -1 : 0;
        score += optBool(input, "pulmonaryCirculation") ? 4 : 0;
        score += optBool(input, "peripheralVascular") ? 2 : 0;
        score += optBool(input, "hypertension") ? 0 : 0;
        score += optBool(input, "paralysis") ? 7 : 0;
        score += optBool(input, "neurologicalDisorders") ? 6 : 0;
        score += optBool(input, "chronicPulmonary") ? 3 : 0;
        score += optBool(input, "diabetesUncomplicated") ? 0 : 0;
        score += optBool(input, "diabetesComplicated") ? 0 : 0;
        score += optBool(input, "hypothyroidism") ? 0 : 0;
        score += optBool(input, "renalFailure") ? 5 : 0;
        score += optBool(input, "liverDisease") ? 11 : 0;
        score += optBool(input, "pepticUlcer") ? 0 : 0;
        score += optBool(input, "lymphoma") ? 9 : 0;
        score += optBool(input, "metastaticCancer") ? 12 : 0;
        score += optBool(input, "solidTumor") ? 4 : 0;
        score += optBool(input, "rheumatoidArthritis") ? 0 : 0;
        score += optBool(input, "coagulopathy") ? 3 : 0;
        score += optBool(input, "obesity") ? -4 : 0;
        score += optBool(input, "weightLoss") ? 6 : 0;
        score += optBool(input, "fluidElectrolyte") ? 5 : 0;
        score += optBool(input, "bloodLossAnemia") ? -2 : 0;
        score += optBool(input, "deficiencyAnemia") ? -2 : 0;
        score += optBool(input, "alcoholAbuse") ? 0 : 0;
        score += optBool(input, "drugAbuse") ? -7 : 0;
        score += optBool(input, "psychoses") ? 0 : 0;
        score += optBool(input, "depression") ? -3 : 0;

        RiskResult result = new RiskResult();
        result.setScore(score);
        result.setRiskLevel(score >= 15 ? "HIGH" : score >= 5 ? "MODERATE" : "LOW");
        result.put("vanWalravenScore", score);
        return result;
    }
}

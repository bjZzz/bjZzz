package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Charlson Comorbidity Index（CCI），含年龄校正。
 */
@Component
public class CharlsonCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "charlson";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        int score = 0;
        score += optBool(input, "myocardialInfarction") ? 1 : 0;
        score += optBool(input, "congestiveHeartFailure") ? 1 : 0;
        score += optBool(input, "peripheralVascularDisease") ? 1 : 0;
        score += optBool(input, "cerebrovascularDisease") ? 1 : 0;
        score += optBool(input, "dementia") ? 1 : 0;
        score += optBool(input, "chronicPulmonaryDisease") ? 1 : 0;
        score += optBool(input, "rheumaticDisease") ? 1 : 0;
        score += optBool(input, "pepticUlcerDisease") ? 1 : 0;
        score += optBool(input, "mildLiverDisease") ? 1 : 0;
        score += optBool(input, "diabetesWithoutComplication") ? 1 : 0;
        score += optBool(input, "diabetesWithComplication") ? 2 : 0;
        score += optBool(input, "hemiplegia") ? 2 : 0;
        score += optBool(input, "renalDisease") ? 2 : 0;
        score += optBool(input, "malignancy") ? 2 : 0;
        score += optBool(input, "moderateSevereLiverDisease") ? 3 : 0;
        score += optBool(input, "metastaticSolidTumor") ? 6 : 0;
        score += optBool(input, "aids") ? 6 : 0;

        double age = optNumber(input, "age", 0);
        int agePoints = 0;
        if (age >= 80) {
            agePoints = 4;
        } else if (age >= 70) {
            agePoints = 3;
        } else if (age >= 60) {
            agePoints = 2;
        } else if (age >= 50) {
            agePoints = 1;
        }
        int total = score + agePoints;

        RiskResult result = new RiskResult();
        result.setScore(total);
        result.setRiskLevel(total >= 5 ? "HIGH" : total >= 3 ? "MODERATE" : "LOW");
        result.put("comorbidityScore", score);
        result.put("agePoints", agePoints);
        double survival10y = Math.pow(0.983, Math.exp(total * 0.9));
        result.put("estimated10YearSurvival", Math.round(survival10y * 1000.0) / 1000.0);
        return result;
    }
}

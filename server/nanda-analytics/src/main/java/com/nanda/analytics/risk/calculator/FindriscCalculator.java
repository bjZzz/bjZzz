package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FINDRISC 2 型糖尿病 10 年风险评分。
 */
@Component
public class FindriscCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "findrisc";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        int score = 0;

        double age = optNumber(input, "age", 0);
        if (age > 64) {
            score += 4;
        } else if (age >= 55) {
            score += 3;
        } else if (age >= 45) {
            score += 2;
        }

        double bmi = optNumber(input, "bmi", 0);
        if (bmi > 30) {
            score += 3;
        } else if (bmi >= 25) {
            score += 1;
        }

        double waist = optNumber(input, "waist", 0);
        boolean female = "F".equalsIgnoreCase(optString(input, "gender", ""))
                || "FEMALE".equalsIgnoreCase(optString(input, "gender", ""));
        if (female) {
            if (waist > 88) {
                score += 4;
            } else if (waist >= 80) {
                score += 3;
            }
        } else {
            if (waist > 102) {
                score += 4;
            } else if (waist >= 94) {
                score += 3;
            }
        }

        score += optBool(input, "physicallyActive") ? 0 : 2;
        score += optBool(input, "dailyVegetables") ? 0 : 1;
        score += optBool(input, "bloodPressureMedication") ? 2 : 0;
        score += optBool(input, "highBloodGlucoseHistory") ? 5 : 0;

        String familyHistory = optString(input, "familyHistory", "NONE");
        if ("FIRST_DEGREE".equalsIgnoreCase(familyHistory)) {
            score += 5;
        } else if ("SECOND_DEGREE".equalsIgnoreCase(familyHistory)) {
            score += 3;
        }

        String level;
        double risk;
        if (score >= 21) {
            level = "VERY_HIGH";
            risk = 50;
        } else if (score >= 15) {
            level = "HIGH";
            risk = 33;
        } else if (score >= 12) {
            level = "MODERATE";
            risk = 17;
        } else if (score >= 7) {
            level = "SLIGHTLY_ELEVATED";
            risk = 4;
        } else {
            level = "LOW";
            risk = 1;
        }

        RiskResult result = new RiskResult();
        result.setScore(score);
        result.setRiskLevel(level);
        result.put("estimated10YearRiskPercent", risk);
        return result;
    }
}

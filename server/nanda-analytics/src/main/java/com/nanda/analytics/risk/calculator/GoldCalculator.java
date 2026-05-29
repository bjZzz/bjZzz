package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GOLD COPD 综合评估（气流受限分级 + ABE 分组）。
 */
@Component
public class GoldCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "gold";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        double fev1 = requireNumber(input, "fev1Percent");
        int grade;
        if (fev1 >= 80) {
            grade = 1;
        } else if (fev1 >= 50) {
            grade = 2;
        } else if (fev1 >= 30) {
            grade = 3;
        } else {
            grade = 4;
        }

        double catScore = optNumber(input, "catScore", 0);
        double mmrc = optNumber(input, "mmrc", 0);
        boolean moreSymptoms = catScore >= 10 || mmrc >= 2;

        double exacerbations = optNumber(input, "exacerbationsLastYear", 0);
        boolean hospitalized = optBool(input, "hospitalizedForExacerbation");
        boolean highRisk = exacerbations >= 2 || hospitalized;

        String group;
        if (highRisk) {
            group = "E";
        } else {
            group = moreSymptoms ? "B" : "A";
        }

        RiskResult result = new RiskResult();
        result.setScore(grade);
        result.setRiskLevel(highRisk ? "HIGH" : moreSymptoms ? "MODERATE" : "LOW");
        result.put("airflowGrade", "GOLD " + grade);
        result.put("abeGroup", group);
        return result;
    }
}

package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * BODE 指数（BMI、气流阻塞、呼吸困难、运动能力），0-10。
 */
@Component
public class BodeCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "bode";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        int score = 0;

        double bmi = requireNumber(input, "bmi");
        score += bmi <= 21 ? 1 : 0;

        double fev1 = requireNumber(input, "fev1Percent");
        if (fev1 < 35) {
            score += 3;
        } else if (fev1 < 50) {
            score += 2;
        } else if (fev1 < 65) {
            score += 1;
        }

        double mmrc = requireNumber(input, "mmrc");
        if (mmrc >= 4) {
            score += 3;
        } else if (mmrc == 3) {
            score += 2;
        } else if (mmrc == 2) {
            score += 1;
        }

        double walk = requireNumber(input, "sixMinuteWalk");
        if (walk < 150) {
            score += 3;
        } else if (walk < 250) {
            score += 2;
        } else if (walk < 350) {
            score += 1;
        }

        String level;
        double survival4y;
        if (score >= 7) {
            level = "VERY_HIGH";
            survival4y = 0.18;
        } else if (score >= 5) {
            level = "HIGH";
            survival4y = 0.57;
        } else if (score >= 3) {
            level = "MODERATE";
            survival4y = 0.67;
        } else {
            level = "LOW";
            survival4y = 0.80;
        }

        RiskResult result = new RiskResult();
        result.setScore(score);
        result.setRiskLevel(level);
        result.put("estimated4YearSurvival", survival4y);
        return result;
    }
}

package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AHA PREVENT（2023）10 年总心血管病风险（简化 logistic 近似）。
 * 以年龄、血脂、血压、eGFR、吸烟、糖尿病为主因子，输出 10 年风险百分比。
 */
@Component
public class PreventCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "prevent";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        double age = requireNumber(input, "age");
        double totalChol = requireNumber(input, "totalCholesterol");
        double hdl = requireNumber(input, "hdl");
        double sbp = requireNumber(input, "systolicBp");
        double egfr = optNumber(input, "egfr", 90);
        boolean treatedBp = optBool(input, "bpTreated");
        boolean smoker = optBool(input, "smoker");
        boolean diabetes = optBool(input, "diabetes");
        boolean female = "F".equalsIgnoreCase(optString(input, "gender", "M"))
                || "FEMALE".equalsIgnoreCase(optString(input, "gender", "M"));

        double nonHdl = totalChol - hdl;

        double logit = -3.5
                + 0.072 * (age - 55)
                + 0.017 * (nonHdl - 130) / 10.0
                + 0.018 * (sbp - 120) / 10.0
                + (treatedBp ? 0.18 : 0)
                + (smoker ? 0.55 : 0)
                + (diabetes ? 0.65 : 0)
                + 0.013 * (90 - Math.min(egfr, 90))
                + (female ? -0.32 : 0);

        double risk = 1.0 / (1.0 + Math.exp(-logit)) * 100;
        risk = Math.max(0, Math.min(100, risk));
        risk = Math.round(risk * 10.0) / 10.0;

        String level;
        if (risk >= 20) {
            level = "HIGH";
        } else if (risk >= 7.5) {
            level = "INTERMEDIATE";
        } else if (risk >= 5) {
            level = "BORDERLINE";
        } else {
            level = "LOW";
        }

        RiskResult result = new RiskResult();
        result.setScore(risk);
        result.setRiskLevel(level);
        result.put("tenYearTotalCvdRiskPercent", risk);
        result.put("nonHdlCholesterol", nonHdl);
        return result;
    }
}

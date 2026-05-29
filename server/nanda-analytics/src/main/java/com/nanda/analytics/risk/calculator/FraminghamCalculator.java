package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Framingham 10 年心血管病风险（D'Agostino 2008 性别专用方程）。
 */
@Component
public class FraminghamCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "framingham";
    }

    @Override
    public RiskResult calculate(Map<String, Object> input) {
        double age = requireNumber(input, "age");
        double totalChol = requireNumber(input, "totalCholesterol");
        double hdl = requireNumber(input, "hdl");
        double sbp = requireNumber(input, "systolicBp");
        boolean treatedBp = optBool(input, "bpTreated");
        boolean smoker = optBool(input, "smoker");
        boolean diabetes = optBool(input, "diabetes");
        boolean female = "F".equalsIgnoreCase(optString(input, "gender", "M"))
                || "FEMALE".equalsIgnoreCase(optString(input, "gender", "M"));

        double lnAge = Math.log(age);
        double lnChol = Math.log(totalChol);
        double lnHdl = Math.log(hdl);
        double lnSbp = Math.log(sbp);

        double sum;
        double baseline;
        double meanSum;
        if (female) {
            sum = 2.32888 * lnAge + 1.20904 * lnChol - 0.70833 * lnHdl
                    + (treatedBp ? 2.82263 * lnSbp : 2.76157 * lnSbp)
                    + (smoker ? 0.52873 : 0) + (diabetes ? 0.69154 : 0);
            baseline = 0.95012;
            meanSum = 26.1931;
        } else {
            sum = 3.06117 * lnAge + 1.12370 * lnChol - 0.93263 * lnHdl
                    + (treatedBp ? 1.99881 * lnSbp : 1.93303 * lnSbp)
                    + (smoker ? 0.65451 : 0) + (diabetes ? 0.57367 : 0);
            baseline = 0.88936;
            meanSum = 23.9802;
        }

        double risk = (1 - Math.pow(baseline, Math.exp(sum - meanSum))) * 100;
        risk = Math.max(0, Math.min(100, risk));
        risk = Math.round(risk * 10.0) / 10.0;

        String level;
        if (risk >= 20) {
            level = "HIGH";
        } else if (risk >= 10) {
            level = "INTERMEDIATE";
        } else {
            level = "LOW";
        }

        RiskResult result = new RiskResult();
        result.setScore(risk);
        result.setRiskLevel(level);
        result.put("tenYearRiskPercent", risk);
        return result;
    }
}

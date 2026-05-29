package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ACC/AHA Pooled Cohort Equations（ASCVD）10 年动脉粥样硬化性心血管病风险。
 * 采用 2013 PCE 系数（白人男/女、非裔男/女四组），输出 10 年风险百分比。
 */
@Component
public class AscvdCalculator implements RiskCalculator {

    @Override
    public String code() {
        return "ascvd";
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
        boolean africanAmerican = "AA".equalsIgnoreCase(optString(input, "race", "WHITE"))
                || "BLACK".equalsIgnoreCase(optString(input, "race", "WHITE"));

        double lnAge = Math.log(age);
        double lnChol = Math.log(totalChol);
        double lnHdl = Math.log(hdl);
        double lnSbp = Math.log(sbp);

        double sum;
        double baseline;
        double meanSum;

        if (female && !africanAmerican) {
            sum = -29.799 * lnAge + 4.884 * lnAge * lnAge + 13.540 * lnChol
                    + (-3.114) * lnAge * lnChol + (-13.578) * lnHdl + 3.149 * lnAge * lnHdl
                    + (treatedBp ? 2.019 * lnSbp : 1.957 * lnSbp)
                    + (smoker ? 7.574 : 0) + (smoker ? -1.665 * lnAge : 0)
                    + (diabetes ? 0.661 : 0);
            baseline = 0.9665;
            meanSum = -29.18;
        } else if (!female && !africanAmerican) {
            sum = 12.344 * lnAge + 11.853 * lnChol + (-2.664) * lnAge * lnChol
                    + (-7.990) * lnHdl + 1.769 * lnAge * lnHdl
                    + (treatedBp ? 1.797 * lnSbp : 1.764 * lnSbp)
                    + (smoker ? 7.837 : 0) + (smoker ? -1.795 * lnAge : 0)
                    + (diabetes ? 0.658 : 0);
            baseline = 0.9144;
            meanSum = 61.18;
        } else if (female) {
            sum = 17.114 * lnAge + 0.940 * lnChol + (-18.920) * lnHdl + 4.475 * lnAge * lnHdl
                    + (treatedBp ? 29.291 * lnSbp - 6.432 * lnAge * lnSbp : 27.820 * lnSbp - 6.087 * lnAge * lnSbp)
                    + (smoker ? 0.691 : 0) + (diabetes ? 0.874 : 0);
            baseline = 0.9533;
            meanSum = 86.61;
        } else {
            sum = 2.469 * lnAge + 0.302 * lnChol + (-0.307) * lnHdl
                    + (treatedBp ? 1.916 * lnSbp : 1.809 * lnSbp)
                    + (smoker ? 0.549 : 0) + (diabetes ? 0.645 : 0);
            baseline = 0.8954;
            meanSum = 19.54;
        }

        double risk = (1 - Math.pow(baseline, Math.exp(sum - meanSum))) * 100;
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
        result.put("tenYearRiskPercent", risk);
        result.put("statinRecommended", risk >= 7.5);
        return result;
    }
}

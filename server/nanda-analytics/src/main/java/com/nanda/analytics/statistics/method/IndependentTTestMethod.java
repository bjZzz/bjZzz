package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.Distributions;
import com.nanda.analytics.statistics.StatMethod;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 独立样本 t 检验（Welch 校正）。
 */
@Component
public class IndependentTTestMethod implements StatMethod {

    @Override
    public String code() {
        return "t_test_independent";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        List<Double> g1 = requireNumberList(input, "group1");
        List<Double> g2 = requireNumberList(input, "group2");

        double mean1 = mean(g1);
        double mean2 = mean(g2);
        double var1 = variance(g1, mean1);
        double var2 = variance(g2, mean2);
        int n1 = g1.size();
        int n2 = g2.size();

        double se = Math.sqrt(var1 / n1 + var2 / n2);
        double t = se == 0 ? 0 : (mean1 - mean2) / se;

        double numerator = Math.pow(var1 / n1 + var2 / n2, 2);
        double denominator = Math.pow(var1 / n1, 2) / (n1 - 1) + Math.pow(var2 / n2, 2) / (n2 - 1);
        double df = denominator == 0 ? (n1 + n2 - 2) : numerator / denominator;
        double pValue = Distributions.twoTailedTP(t, df);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("mean1", round(mean1));
        result.put("mean2", round(mean2));
        result.put("meanDifference", round(mean1 - mean2));
        result.put("tStatistic", round(t));
        result.put("degreesOfFreedom", round(df));
        result.put("pValue", round(pValue));
        result.put("significant", pValue < 0.05);
        result.put("method", "Welch's t-test");
        return result;
    }

    private double mean(List<Double> values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private double variance(List<Double> values, double mean) {
        if (values.size() < 2) {
            return 0;
        }
        double sse = 0;
        for (double v : values) {
            sse += (v - mean) * (v - mean);
        }
        return sse / (values.size() - 1);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

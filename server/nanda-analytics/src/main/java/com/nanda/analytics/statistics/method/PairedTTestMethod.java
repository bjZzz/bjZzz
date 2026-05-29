package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.Distributions;
import com.nanda.analytics.statistics.StatMethod;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PairedTTestMethod implements StatMethod {

    @Override
    public String code() {
        return "t_test_paired";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        List<Double> before = requireNumberList(input, "before");
        List<Double> after = requireNumberList(input, "after");
        if (before.size() != after.size()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "before 与 after 长度不一致");
        }
        int n = before.size();
        if (n < 2) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "样本量过小");
        }

        double sumDiff = 0;
        double[] diffs = new double[n];
        for (int i = 0; i < n; i++) {
            diffs[i] = after.get(i) - before.get(i);
            sumDiff += diffs[i];
        }
        double meanDiff = sumDiff / n;
        double sse = 0;
        for (double d : diffs) {
            sse += (d - meanDiff) * (d - meanDiff);
        }
        double sd = Math.sqrt(sse / (n - 1));
        double se = sd / Math.sqrt(n);
        double t = se == 0 ? 0 : meanDiff / se;
        double df = n - 1;
        double pValue = Distributions.twoTailedTP(t, df);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("n", n);
        result.put("meanDifference", round(meanDiff));
        result.put("sdDifference", round(sd));
        result.put("tStatistic", round(t));
        result.put("degreesOfFreedom", df);
        result.put("pValue", round(pValue));
        result.put("significant", pValue < 0.05);
        return result;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

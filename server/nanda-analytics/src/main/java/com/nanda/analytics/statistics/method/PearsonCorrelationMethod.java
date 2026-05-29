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
public class PearsonCorrelationMethod implements StatMethod {

    @Override
    public String code() {
        return "correlation_pearson";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        List<Double> x = requireNumberList(input, "x");
        List<Double> y = requireNumberList(input, "y");
        if (x.size() != y.size()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "x 与 y 长度不一致");
        }
        int n = x.size();
        if (n < 3) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "样本量过小，至少需要 3 对观测");
        }

        double meanX = mean(x);
        double meanY = mean(y);
        double sxy = 0;
        double sxx = 0;
        double syy = 0;
        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - meanX;
            double dy = y.get(i) - meanY;
            sxy += dx * dy;
            sxx += dx * dx;
            syy += dy * dy;
        }
        double r = (sxx == 0 || syy == 0) ? 0 : sxy / Math.sqrt(sxx * syy);
        r = Math.max(-1.0, Math.min(1.0, r));

        double t = r * Math.sqrt((n - 2) / Math.max(1e-12, (1 - r * r)));
        double pValue = Distributions.twoTailedTP(t, n - 2);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("n", n);
        result.put("pearsonR", round(r));
        result.put("rSquared", round(r * r));
        result.put("tStatistic", round(t));
        result.put("pValue", round(pValue));
        result.put("significant", pValue < 0.05);
        return result;
    }

    private double mean(List<Double> values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

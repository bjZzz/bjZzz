package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.StatMethod;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单因素方差分析（One-way ANOVA），返回 F 统计量。
 */
@Component
public class OneWayAnovaMethod implements StatMethod {

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        Object groupsObj = input != null ? input.get("groups") : null;
        if (!(groupsObj instanceof List)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少分组参数: groups (二维数组)");
        }
        List<List<Object>> rawGroups = (List<List<Object>>) groupsObj;
        if (rawGroups.size() < 2) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "至少需要 2 组");
        }

        List<List<Double>> groups = new ArrayList<List<Double>>();
        double grandSum = 0;
        int totalN = 0;
        for (List<Object> raw : rawGroups) {
            List<Double> group = new ArrayList<Double>();
            for (Object item : raw) {
                double v = Double.parseDouble(String.valueOf(item));
                group.add(v);
                grandSum += v;
                totalN++;
            }
            if (group.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "存在空分组");
            }
            groups.add(group);
        }
        double grandMean = grandSum / totalN;
        int k = groups.size();

        double ssBetween = 0;
        double ssWithin = 0;
        for (List<Double> group : groups) {
            double sum = 0;
            for (double v : group) {
                sum += v;
            }
            double mean = sum / group.size();
            ssBetween += group.size() * (mean - grandMean) * (mean - grandMean);
            for (double v : group) {
                ssWithin += (v - mean) * (v - mean);
            }
        }
        int dfBetween = k - 1;
        int dfWithin = totalN - k;
        double msBetween = ssBetween / dfBetween;
        double msWithin = dfWithin > 0 ? ssWithin / dfWithin : 0;
        double f = msWithin == 0 ? 0 : msBetween / msWithin;

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("groups", k);
        result.put("totalN", totalN);
        result.put("ssBetween", round(ssBetween));
        result.put("ssWithin", round(ssWithin));
        result.put("dfBetween", dfBetween);
        result.put("dfWithin", dfWithin);
        result.put("fStatistic", round(f));
        return result;
    }

    @Override
    public String code() {
        return "anova_one_way";
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

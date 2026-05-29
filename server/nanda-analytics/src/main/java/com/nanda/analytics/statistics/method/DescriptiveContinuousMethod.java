package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.StatMethod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DescriptiveContinuousMethod implements StatMethod {

    @Override
    public String code() {
        return "descriptive_continuous";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        List<Double> values = new ArrayList<Double>(requireNumberList(input, "values"));
        Collections.sort(values);
        int n = values.size();

        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        double mean = sum / n;

        double sse = 0;
        for (double v : values) {
            sse += (v - mean) * (v - mean);
        }
        double variance = n > 1 ? sse / (n - 1) : 0;
        double sd = Math.sqrt(variance);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("n", n);
        result.put("mean", round(mean));
        result.put("sd", round(sd));
        result.put("variance", round(variance));
        result.put("min", values.get(0));
        result.put("max", values.get(n - 1));
        result.put("median", round(percentile(values, 50)));
        result.put("q1", round(percentile(values, 25)));
        result.put("q3", round(percentile(values, 75)));
        result.put("sem", round(n > 0 ? sd / Math.sqrt(n) : 0));
        return result;
    }

    private double percentile(List<Double> sorted, double p) {
        if (sorted.size() == 1) {
            return sorted.get(0);
        }
        double rank = p / 100.0 * (sorted.size() - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        double weight = rank - lower;
        return sorted.get(lower) * (1 - weight) + sorted.get(upper) * weight;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

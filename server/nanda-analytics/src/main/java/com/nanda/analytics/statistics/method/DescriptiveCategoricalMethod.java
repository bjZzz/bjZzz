package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.StatMethod;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DescriptiveCategoricalMethod implements StatMethod {

    @Override
    public String code() {
        return "descriptive_categorical";
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        Object value = input != null ? input.get("values") : null;
        if (!(value instanceof List)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少分类数组参数: values");
        }
        List<?> values = (List<?>) value;
        if (values.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "分类数组为空");
        }

        Map<String, Integer> frequency = new LinkedHashMap<String, Integer>();
        for (Object item : values) {
            String key = item != null ? String.valueOf(item) : "null";
            Integer count = frequency.get(key);
            frequency.put(key, count == null ? 1 : count + 1);
        }

        Map<String, Object> proportions = new LinkedHashMap<String, Object>();
        int total = values.size();
        for (Map.Entry<String, Integer> entry : frequency.entrySet()) {
            proportions.put(entry.getKey(), Math.round((double) entry.getValue() / total * 10000.0) / 10000.0);
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("n", total);
        result.put("categories", frequency.size());
        result.put("frequency", frequency);
        result.put("proportion", proportions);
        return result;
    }
}

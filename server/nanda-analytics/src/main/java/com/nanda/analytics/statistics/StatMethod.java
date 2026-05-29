package com.nanda.analytics.statistics;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface StatMethod {

    String code();

    Map<String, Object> execute(Map<String, Object> input);

    default List<Double> requireNumberList(Map<String, Object> input, String key) {
        Object value = input != null ? input.get(key) : null;
        if (!(value instanceof List)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少数值数组参数: " + key);
        }
        List<?> raw = (List<?>) value;
        List<Double> numbers = new ArrayList<Double>();
        for (Object item : raw) {
            if (item == null) {
                continue;
            }
            try {
                numbers.add(Double.parseDouble(String.valueOf(item)));
            } catch (NumberFormatException ex) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "数组含非数值元素: " + key);
            }
        }
        if (numbers.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "数值数组为空: " + key);
        }
        return numbers;
    }
}

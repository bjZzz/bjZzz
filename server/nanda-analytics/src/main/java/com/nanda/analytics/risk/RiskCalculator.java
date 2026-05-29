package com.nanda.analytics.risk;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;

import java.util.Map;

public interface RiskCalculator {

    String code();

    RiskResult calculate(Map<String, Object> input);

    default double requireNumber(Map<String, Object> input, String key) {
        Object value = input != null ? input.get(key) : null;
        if (value == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少必填参数: " + key);
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "参数格式错误: " + key);
        }
    }

    default double optNumber(Map<String, Object> input, String key, double defaultValue) {
        Object value = input != null ? input.get(key) : null;
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    default boolean optBool(Map<String, Object> input, String key) {
        Object value = input != null ? input.get(key) : null;
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(text) || "1".equals(text) || "yes".equalsIgnoreCase(text);
    }

    default String optString(Map<String, Object> input, String key, String defaultValue) {
        Object value = input != null ? input.get(key) : null;
        return value != null ? String.valueOf(value) : defaultValue;
    }
}

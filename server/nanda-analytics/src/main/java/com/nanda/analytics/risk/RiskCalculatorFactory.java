package com.nanda.analytics.risk;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RiskCalculatorFactory {

    private final Map<String, RiskCalculator> calculators = new HashMap<String, RiskCalculator>();

    public RiskCalculatorFactory(List<RiskCalculator> calculatorBeans) {
        for (RiskCalculator calculator : calculatorBeans) {
            calculators.put(calculator.code(), calculator);
        }
    }

    public RiskCalculator get(String modelCode) {
        RiskCalculator calculator = calculators.get(modelCode);
        if (calculator == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "不支持的风险模型: " + modelCode);
        }
        return calculator;
    }

    public List<String> supportedModels() {
        return new ArrayList<String>(calculators.keySet());
    }
}

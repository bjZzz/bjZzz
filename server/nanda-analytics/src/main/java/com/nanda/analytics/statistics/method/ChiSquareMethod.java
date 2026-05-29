package com.nanda.analytics.statistics.method;

import com.nanda.analytics.statistics.Distributions;
import com.nanda.analytics.statistics.StatMethod;
import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡方独立性检验（r×c 列联表）。
 */
@Component
public class ChiSquareMethod implements StatMethod {

    @Override
    public String code() {
        return "chi_square";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> input) {
        Object tableObj = input != null ? input.get("table") : null;
        if (!(tableObj instanceof List)) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "缺少列联表参数: table");
        }
        List<List<Object>> rawTable = (List<List<Object>>) tableObj;
        int rows = rawTable.size();
        if (rows < 2) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "列联表至少需要 2 行");
        }
        int cols = rawTable.get(0).size();
        double[][] observed = new double[rows][cols];
        double[] rowTotals = new double[rows];
        double[] colTotals = new double[cols];
        double grandTotal = 0;

        for (int i = 0; i < rows; i++) {
            if (rawTable.get(i).size() != cols) {
                throw new BusinessException(ErrorCode.PARAM_INVALID, "列联表各行列数不一致");
            }
            for (int j = 0; j < cols; j++) {
                double v = Double.parseDouble(String.valueOf(rawTable.get(i).get(j)));
                observed[i][j] = v;
                rowTotals[i] += v;
                colTotals[j] += v;
                grandTotal += v;
            }
        }
        if (grandTotal == 0) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "列联表合计为 0");
        }

        double chiSquare = 0;
        double minExpected = Double.MAX_VALUE;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double expected = rowTotals[i] * colTotals[j] / grandTotal;
                minExpected = Math.min(minExpected, expected);
                if (expected > 0) {
                    chiSquare += Math.pow(observed[i][j] - expected, 2) / expected;
                }
            }
        }
        int df = (rows - 1) * (cols - 1);
        double pValue = Distributions.chiSquareUpperP(chiSquare, df);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("chiSquare", round(chiSquare));
        result.put("degreesOfFreedom", df);
        result.put("pValue", round(pValue));
        result.put("significant", pValue < 0.05);
        result.put("minExpectedCount", round(minExpected));
        result.put("expectedCountWarning", minExpected < 5);
        return result;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}

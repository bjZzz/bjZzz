package com.nanda.analytics.risk.calculator;

import com.nanda.analytics.risk.RiskCalculator;
import com.nanda.analytics.risk.RiskResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 共病网络挖掘 / 因果效应分析等复杂模型，计算下沉到沙箱（W8 联调）。
 * 当前返回受理标记，保证接口可用且语义明确。
 */
@Component
public class SandboxDelegatedCalculator {

    @Component
    public static class ComorbidityNetwork implements RiskCalculator {
        @Override
        public String code() {
            return "comorbidity-network";
        }

        @Override
        public RiskResult calculate(Map<String, Object> input) {
            RiskResult result = new RiskResult();
            result.setScore(0);
            result.setRiskLevel("PENDING_SANDBOX");
            result.put("delegatedTo", "sandbox");
            result.put("message", "共病网络挖掘需在沙箱执行（W8）");
            return result;
        }
    }

    @Component
    public static class CausalAnalysis implements RiskCalculator {
        @Override
        public String code() {
            return "causal-analysis";
        }

        @Override
        public RiskResult calculate(Map<String, Object> input) {
            RiskResult result = new RiskResult();
            result.setScore(0);
            result.setRiskLevel("PENDING_SANDBOX");
            result.put("delegatedTo", "sandbox");
            result.put("message", "因果效应分析需在沙箱执行（W8）");
            return result;
        }
    }
}

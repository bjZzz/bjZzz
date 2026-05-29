package com.nanda.analytics.statistics;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统计方法注册表：简单方法走 Java 实现，复杂方法（生存/回归）下沉沙箱。
 */
@Component
public class StatMethodRegistry {

    private final Map<String, StatMethod> javaMethods = new HashMap<String, StatMethod>();

    private static final Set<String> SANDBOX_METHODS = new HashSet<String>(Arrays.asList(
            "kaplan_meier", "cox_regression", "logistic_regression", "linear_regression",
            "mann_whitney_u", "wilcoxon_signed_rank", "kruskal_wallis", "log_rank_test",
            "propensity_score_matching", "mixed_effects_model"
    ));

    public StatMethodRegistry(List<StatMethod> methodBeans) {
        for (StatMethod method : methodBeans) {
            javaMethods.put(method.code(), method);
        }
    }

    public boolean isJavaMethod(String code) {
        return javaMethods.containsKey(code);
    }

    public boolean isSandboxMethod(String code) {
        return SANDBOX_METHODS.contains(code);
    }

    public StatMethod getJavaMethod(String code) {
        return javaMethods.get(code);
    }

    public Map<String, Object> listMethods() {
        Map<String, Object> all = new LinkedHashMap<String, Object>();
        all.put("java", new ArrayList<String>(javaMethods.keySet()));
        all.put("sandbox", new ArrayList<String>(SANDBOX_METHODS));
        return all;
    }
}

package com.nanda.research.project;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<String, Set<String>>();

    static {
        TRANSITIONS.put("DRAFT", set("APPROVED"));
        TRANSITIONS.put("APPROVED", set("EXECUTING"));
        TRANSITIONS.put("EXECUTING", set("CLOSING"));
        TRANSITIONS.put("CLOSING", set("ARCHIVED"));
    }

    public void validate(String current, String target) {
        if (current == null || target == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "状态不能为空");
        }
        if (current.equals(target)) {
            return;
        }
        Set<String> allowed = TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BusinessException(ErrorCode.CONFLICT,
                    "不允许从 " + current + " 转换到 " + target);
        }
    }

    private static Set<String> set(String... values) {
        Set<String> result = new HashSet<String>();
        Collections.addAll(result, values);
        return result;
    }
}

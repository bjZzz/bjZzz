package com.nanda.asset.domain.enums;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;

import java.util.Locale;

public enum SpecialtyType {

    METABOLIC("metabolic"),
    CARDIO_CEREBROVASCULAR("cardio_cerebrovascular"),
    RESPIRATORY("respiratory");

    private final String pathCode;

    SpecialtyType(String pathCode) {
        this.pathCode = pathCode;
    }

    public String getPathCode() {
        return pathCode;
    }

    public static SpecialtyType fromPath(String pathType) {
        if (pathType == null || pathType.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "专病类型不能为空");
        }
        String normalized = pathType.toLowerCase(Locale.ROOT);
        for (SpecialtyType type : values()) {
            if (type.pathCode.equals(normalized) || type.name().equalsIgnoreCase(pathType)) {
                return type;
            }
        }
        throw new BusinessException(ErrorCode.PARAM_INVALID, "不支持的专病类型: " + pathType);
    }
}

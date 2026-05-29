package com.nanda.ingestion.adapter;

import com.nanda.common.core.exception.BusinessException;
import com.nanda.common.core.exception.ErrorCode;
import com.nanda.common.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdapterRegistry {

    private final List<DataSourceAdapter> adapters;

    public AdapterRegistry(List<DataSourceAdapter> adapters) {
        this.adapters = adapters;
    }

    public DataSourceAdapter getAdapter(String protocol) {
        for (DataSourceAdapter adapter : adapters) {
            if (adapter.supports(protocol)) {
                return adapter;
            }
        }
        throw new BusinessException(ErrorCode.BUSINESS_RULE, "不支持的协议: " + protocol);
    }

    public DataSourceConfig parseConfig(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new DataSourceConfig();
        }
        return JsonUtils.fromJson(configJson, DataSourceConfig.class);
    }
}

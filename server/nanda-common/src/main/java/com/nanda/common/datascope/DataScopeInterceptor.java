package com.nanda.common.datascope;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Placeholder for MyBatis data-scope SQL injection (implement in platform module).
 */
@Slf4j
@Component
public class DataScopeInterceptor {

    public String buildOrgFilter(String orgAlias) {
        log.trace("DataScope placeholder for alias={}", orgAlias);
        return "";
    }
}

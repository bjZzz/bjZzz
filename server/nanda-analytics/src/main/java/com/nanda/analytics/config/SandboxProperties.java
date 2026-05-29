package com.nanda.analytics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nanda.sandbox")
public class SandboxProperties {

    /**
     * 是否启用 Python ComputePlane 联调。
     */
    private boolean enabled = false;

    /**
     * Python 沙箱内网地址，如 http://sandbox:8000
     */
    private String baseUrl = "http://127.0.0.1:8000";

    /**
     * Java BFF 调用 Python 的内部令牌。
     */
    private String internalToken = "nanda-sandbox-dev-token";

    private int connectTimeoutMs = 3000;

    private int readTimeoutMs = 30000;
}

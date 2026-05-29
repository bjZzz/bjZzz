package com.nanda.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nanda.jwt")
public class JwtProperties {

    private String secret = "nanda-dev-secret-change-in-production-min-32-chars";
    private long accessExpireSeconds = 7200L;
    private long refreshExpireSeconds = 604800L;
}

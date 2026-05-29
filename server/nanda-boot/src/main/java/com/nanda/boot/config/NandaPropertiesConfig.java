package com.nanda.boot.config;

import com.nanda.common.config.NandaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NandaProperties.class)
public class NandaPropertiesConfig {
}

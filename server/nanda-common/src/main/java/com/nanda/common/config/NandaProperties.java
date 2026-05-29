package com.nanda.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nanda")
public class NandaProperties {

    private Mq mq = new Mq();
    private Redis redis = new Redis();
    private Elasticsearch elasticsearch = new Elasticsearch();

    @Data
    public static class Mq {
        private boolean enabled = false;
    }

    @Data
    public static class Redis {
        private boolean enabled = false;
    }

    @Data
    public static class Elasticsearch {
        private boolean enabled = false;
    }
}

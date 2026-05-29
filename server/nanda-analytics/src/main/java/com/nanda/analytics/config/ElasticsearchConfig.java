package com.nanda.analytics.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "nanda.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(
            @Value("${spring.elasticsearch.rest.uris:http://localhost:9200}") String uris) {
        String uri = uris.split(",")[0].trim();
        String withoutScheme = uri.replace("http://", "").replace("https://", "");
        String[] parts = withoutScheme.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
    }
}

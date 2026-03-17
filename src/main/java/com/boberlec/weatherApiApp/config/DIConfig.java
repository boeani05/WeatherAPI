package com.boberlec.weatherApiApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class DIConfig {

    @Bean
    public RestClient returnRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    public RedisTemplate<String, Object> returnRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}

package com.boberlec.weatherApiApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DIConfig {

    @Bean
    public RestClient returnRestClient() {
        return RestClient.builder().build();
    }
}

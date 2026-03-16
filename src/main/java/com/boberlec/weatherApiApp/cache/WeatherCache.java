package com.boberlec.weatherApiApp.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WeatherCache {

    // inject class redistemplate with constructor
    private final RedisTemplate<String, Object> redisTemplate;

    public WeatherCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(String city) {

    }
}

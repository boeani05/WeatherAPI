package com.boberlec.weatherApiApp.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Service
public class WeatherCache {

    // inject class redistemplate with constructor
    private final RedisTemplate<String, Object> redisTemplate;

    // constructor for getting redistemplate
    public WeatherCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // building key to get weatherdata, if requested
    private String buildKey(String city) {
        return "weather:" + normalizeCity(city);
    }

    // method to get weather from cache
    @SuppressWarnings("unchecked")
    public Map<String, Object> getWeatherFromCache(String city) {
        return (Map<String, Object>) redisTemplate
                .opsForValue()
                .get(buildKey(city));
    }

    // method to save non-saved weather-data in cache
    public void saveWeatherToCache(String city, Map<String, Object> data) {
        redisTemplate
                .opsForValue()
                .set(buildKey(city), data, Expiration.from(Duration.ofHours(1)));
    }

    // if city is entered like "     New York      ", it is not seperatedly saved in the cache -> only using one "new york"
    private String normalizeCity(String city) {
        return city
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}

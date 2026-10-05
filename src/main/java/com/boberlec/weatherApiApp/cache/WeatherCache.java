package com.boberlec.weatherApiApp.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Service
public class WeatherCache {

    private static final Logger log = LoggerFactory.getLogger(WeatherCache.class);

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
    // the cache is only an optimization: if redis is down or the entry is unreadable, this acts like a cache miss
    @SuppressWarnings("unchecked")
    public Map<String, Object> getWeatherFromCache(String city) {
        try {
            Object cachedWeatherData = redisTemplate
                    .opsForValue()
                    .get(buildKey(city));

            if (cachedWeatherData instanceof Map<?, ?> cachedWeatherDataMap) {
                return (Map<String, Object>) cachedWeatherDataMap;
            }

            return null;
        } catch (DataAccessException | SerializationException e) {
            log.warn("Could not read weather of '{}' from cache, asking the weather api instead: {}", city, e.getMostSpecificCause().getMessage());
            return null;
        }
    }

    // method to save non-saved weather-data in cache
    public void saveWeatherToCache(String city, Map<String, Object> data) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(buildKey(city), data, Expiration.from(Duration.ofHours(1)));
        } catch (DataAccessException | SerializationException e) {
            log.warn("Could not save weather of '{}' to cache: {}", city, e.getMostSpecificCause().getMessage());
        }
    }

    // if city is entered like "     New York      ", it is not seperatedly saved in the cache -> only using one "new york"
    private String normalizeCity(String city) {
        return city
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}

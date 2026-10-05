package com.boberlec.weatherApiApp.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class WeatherCacheTest {

    // a redis template that behaves like a redis server that is not running
    private final RedisTemplate<String, Object> redisIsDown = new RedisTemplate<>() {
        @Override
        public ValueOperations<String, Object> opsForValue() {
            throw new RedisConnectionFailureException("Unable to connect to Redis");
        }
    };

    private final WeatherCache weatherCache = new WeatherCache(redisIsDown);

    @Test
    void readingWithoutRedisIsACacheMiss() {
        assertThat(weatherCache.getWeatherFromCache("Vienna")).isNull();
    }

    @Test
    void savingWithoutRedisDoesNotFail() {
        assertThatCode(() -> weatherCache.saveWeatherToCache("Vienna", Map.of("city", "Wien")))
                .doesNotThrowAnyException();
    }
}

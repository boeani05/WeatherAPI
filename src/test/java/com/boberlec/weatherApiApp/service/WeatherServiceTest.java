package com.boberlec.weatherApiApp.service;

import com.boberlec.weatherApiApp.cache.WeatherCache;
import com.boberlec.weatherApiApp.client.WeatherApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    private final WeatherApiClient weatherApiClient = mock(WeatherApiClient.class);
    private final WeatherCache weatherCache = mock(WeatherCache.class);
    private final WeatherService weatherService = new WeatherService(weatherApiClient, weatherCache);

    private static final Map<String, Object> ANSWER_OF_WEATHER_API = Map.of(
            "resolvedAddress", "Wien, Österreich",
            "description", "Cooling down with rain.",
            "days", List.of(Map.of("temp", 12.3, "conditions", "Rain", "datetime", "2026-10-05"))
    );

    // a mockito mock would answer with an empty map, which the service reads as a cache hit
    @BeforeEach
    void cacheIsEmpty() {
        when(weatherCache.getWeatherFromCache(any())).thenReturn(null);
    }

    @Test
    void cacheHitIsReturnedWithoutAskingTheWeatherApi() {
        Map<String, Object> cached = Map.of("city", "Wien, Österreich");
        when(weatherCache.getWeatherFromCache("Vienna")).thenReturn(cached);

        assertThat(weatherService.getWeather("Vienna")).isSameAs(cached);

        verify(weatherApiClient, never()).getWeather(any());
    }

    @Test
    void cacheMissAsksTheWeatherApiAndSavesTheResult() {
        when(weatherApiClient.getWeather("Vienna")).thenReturn(ANSWER_OF_WEATHER_API);

        Map<String, Object> weather = weatherService.getWeather("Vienna");

        assertThat(weather)
                .containsEntry("city", "Wien, Österreich")
                .containsEntry("description", "Cooling down with rain.")
                .containsEntry("temperature", 12.3)
                .containsEntry("conditions", "Rain")
                .containsEntry("datetime", "2026-10-05");
        verify(weatherCache).saveWeatherToCache("Vienna", weather);
    }

    @Test
    void incompleteAnswerIsReturnedButNotCached() {
        when(weatherApiClient.getWeather("Vienna")).thenReturn(Map.of("resolvedAddress", "Wien, Österreich"));

        Map<String, Object> weather = weatherService.getWeather("Vienna");

        assertThat(weather)
                .containsEntry("city", "Wien, Österreich")
                .containsEntry("temperature", null);
        verify(weatherCache, never()).saveWeatherToCache(any(), any());
    }

    @Test
    void cityIsTrimmedForCacheAndWeatherApi() {
        when(weatherApiClient.getWeather("Vienna")).thenReturn(ANSWER_OF_WEATHER_API);

        weatherService.getWeather("   Vienna   ");

        verify(weatherCache).getWeatherFromCache("Vienna");
        verify(weatherApiClient).getWeather("Vienna");
    }
}

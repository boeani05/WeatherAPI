package com.boberlec.weatherApiApp.service;

import com.boberlec.weatherApiApp.cache.WeatherCache;
import com.boberlec.weatherApiApp.client.WeatherApiClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// task: delegate tasks to other classes (e.g. get weather data, save in cache etc.)
@Service
public class WeatherService {

    // di of weather api client/weather cache
    private final WeatherApiClient weatherApiClient;
    private final WeatherCache weatherCache;

    private static final String NO_ADDRESS = "No address available!";
    private static final String NO_DESCRIPTION = "No description available!";
    private static final String NO_CONDITIONS = "No conditions available!";
    private static final String NO_TIME = "No time available!";

    public WeatherService(WeatherApiClient weatherApiClient, WeatherCache weatherCache) {
        this.weatherApiClient = weatherApiClient;
        this.weatherCache = weatherCache;
    }

    public Map<String, Object> getWeather(String requestedCity) {

        // trimmed once here, so that cache and weather api work with the same city
        String city = requestedCity.trim();

        Map<String, Object> cachedWeatherData = weatherCache.getWeatherFromCache(city);

        // MISS case, no city found in cache
        if (cachedWeatherData == null) {

            Map<String, Object> weatherOfApiClientCall = weatherApiClient.getWeather(city);
            Map<String, Object> smallerResponseOfApiClientCall = createDefaultResult(weatherOfApiClientCall);

            // calls method of weatherapiclient to get weather data (not responsibility of service)
            Optional<Map<String, Object>> firstDay = firstDay(weatherOfApiClientCall);

            firstDay.ifPresent(day -> {
                smallerResponseOfApiClientCall.put("temperature", day.get("temp"));
                smallerResponseOfApiClientCall.put("conditions", day.getOrDefault("conditions", NO_CONDITIONS));
                smallerResponseOfApiClientCall.put("datetime", day.getOrDefault("datetime", NO_TIME));
            });

            // only complete data goes into the cache - otherwise the placeholder texts
            // would be served for a whole hour, even if the weather api works again
            if (firstDay.isPresent()) {
                weatherCache.saveWeatherToCache(city, smallerResponseOfApiClientCall);
            }

            return smallerResponseOfApiClientCall;
        }

        // will be put out, if city is found within cache
        return cachedWeatherData;

    }

    // default vanilla content of weather data (will be used, if something goes wrong)
    private Map<String, Object> createDefaultResult(Map<String, Object> weatherOfApiClientCall) {
        if (weatherOfApiClientCall == null) {
            weatherOfApiClientCall = Map.of();
        }

        Map<String, Object> smallerResponseOfApiClientCall = new HashMap<>();

        smallerResponseOfApiClientCall.put("city", weatherOfApiClientCall.getOrDefault("resolvedAddress", NO_ADDRESS));
        smallerResponseOfApiClientCall.put("description", weatherOfApiClientCall.getOrDefault("description", NO_DESCRIPTION));
        // null and not 0.0: zero degrees is a real temperature, "unknown" must not look like one
        smallerResponseOfApiClientCall.put("temperature", null);
        smallerResponseOfApiClientCall.put("conditions", NO_CONDITIONS);
        smallerResponseOfApiClientCall.put("datetime", NO_TIME);

        return smallerResponseOfApiClientCall;
    }

    // puts out only the first day of the requested weather data
    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> firstDay(Map<String, Object> weatherOfApiClientCall) {
        Object retrievedDaysCategory = weatherOfApiClientCall.get("days");

        if (!(retrievedDaysCategory instanceof List<?> days) || days.isEmpty()) {
            return Optional.empty();
        }

        Object firstDayOfRetrievedDays = days.getFirst();

        if (firstDayOfRetrievedDays instanceof Map<?, ?> firstDayOfRetrievedDaysMap) {
            return Optional.of((Map<String, Object>) firstDayOfRetrievedDaysMap);
        }

        return Optional.empty();
    }
}
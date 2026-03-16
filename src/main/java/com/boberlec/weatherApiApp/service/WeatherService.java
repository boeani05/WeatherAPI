package com.boberlec.weatherApiApp.service;

import com.boberlec.weatherApiApp.client.WeatherApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// task: delegate tasks to other classes (e.g. get weather data, save in cache etc.)
@Service
public class WeatherService {

    // di of weather api client
    private final WeatherApiClient weatherApiClient;

    private static final String NO_ADDRESS = "No address available!";
    private static final String NO_DESCRIPTION = "No description available!";
    private static final String NO_CONDITIONS = "No conditions available!";
    private static final String NO_TIME = "No time available!";

    public WeatherService(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    public Map<String, Object> getWeather(String city) {
        // calls method of weatherapiclient to get weather data (not responsibility of service)
        Map<String, Object> weatherOfApiClientCall = weatherApiClient.getWeather(city);
        Map<String, Object> smallerResponseOfApiClientCall = createDefaultResult(weatherOfApiClientCall);

        firstDay(weatherOfApiClientCall).ifPresent(day -> {
            smallerResponseOfApiClientCall.put("temperature", day.getOrDefault("temp", 0.0));
            smallerResponseOfApiClientCall.put("conditions", day.getOrDefault("conditions", NO_CONDITIONS));
            smallerResponseOfApiClientCall.put("datetime", day.getOrDefault("datetime", NO_TIME));
        });

        return smallerResponseOfApiClientCall;
    }

    private Map<String, Object> createDefaultResult(Map<String, Object> weatherOfApiClientCall) {
        Map<String, Object> smallerResponseOfApiClientCall = new HashMap<>();

        smallerResponseOfApiClientCall.put("city", weatherOfApiClientCall.getOrDefault("resolvedAddress", NO_ADDRESS));
        smallerResponseOfApiClientCall.put("description", weatherOfApiClientCall.getOrDefault("description", NO_DESCRIPTION));
        smallerResponseOfApiClientCall.put("temperature", 0.0);
        smallerResponseOfApiClientCall.put("conditions", NO_CONDITIONS);
        smallerResponseOfApiClientCall.put("datetime", NO_TIME);

        return smallerResponseOfApiClientCall;
    }

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

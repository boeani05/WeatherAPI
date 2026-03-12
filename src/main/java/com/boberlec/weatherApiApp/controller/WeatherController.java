package com.boberlec.weatherApiApp.controller;

import com.boberlec.weatherApiApp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    // Add endpoint for /weather?city={city}
    @GetMapping("/weather")
    public Map<String, Object> getWeather(
            // The city parameter
            @RequestParam(name = "city") String city
    ) {
        // Basic error handling
        if (city.isBlank()) {
            throw new IllegalArgumentException("\nCity must not be empty.");
        }


        // controller calls weatherService to save weather data in a map (spring converts it into json)
        Map<String, Object> weatherData = weatherService.getWeather(city);

        return weatherData;
    }
}

package com.boberlec.weatherApiApp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class WeatherController {

    // Add endpoint for /weather?city={city}
    @GetMapping("/weather")
    public String getWeather(
            // The city parameter
            @RequestParam String city
    ) {
        // Basic error handling
        if (city.isEmpty()) {
            throw new IllegalArgumentException("\nCity must not be empty.");
        }

        // demo output for testing
        return String.format("""
                        city: %s,
                        source: hardcoded,
                        timestamp: %s
                        """,
                city,
                LocalDateTime.now()
        );
    }
}

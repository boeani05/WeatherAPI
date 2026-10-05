package com.boberlec.weatherApiApp.client;

import com.boberlec.weatherApiApp.exceptions.CityNotFoundException;
import com.boberlec.weatherApiApp.exceptions.WeatherClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

// task: weatherapiclient has the task to provide classes (e.g. service) with weather data
@Component
public class WeatherApiClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);

    // load a restclient from config class
    private final RestClient restClient;

    // api key and api url from application.properties
    private final String apiKey;
    private final String apiUrl;

    public WeatherApiClient(
            RestClient restClient,
            @Value("${weather.api.key}") String apiKey,
            @Value("${weather.api.base-url}") String apiUrl
    ) {
        // without a key every request would fail with 401 - better to stop at startup with a clear message
        if (apiKey.isBlank()) {
            throw new IllegalStateException(
                    "weather.api.key is empty - set it in application.properties or as environment variable WEATHER_API_KEY");
        }

        this.restClient = restClient;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWeather(String city) {
        try {
            Map<String, Object> responseBody = restClient.get()
                    // spring encodes the uri variables itself - encoding the city by hand as well
                    // would encode it twice ("New York" -> "New%2520York")
                    .uri(apiUrl + "/{city}?unitGroup=metric&contentType=json&key={key}", city, apiKey)
                    .retrieve()
                    .body(Map.class);

            if (responseBody == null) {
                throw new WeatherClientException("Weather API returned empty response");
            }

            return responseBody;
        } catch (RestClientResponseException e) {
            log.warn("Weather API answered {} for city '{}': {}", e.getStatusCode(), city, e.getResponseBodyAsString());

            // the weather api answers 400 for a location it does not know
            int status = e.getStatusCode().value();
            if (status == 400 || status == 404) {
                throw new CityNotFoundException(city);
            }

            throw new WeatherClientException("Weather API error: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new WeatherClientException("Can't reach Weather API", e);
        }
    }

}

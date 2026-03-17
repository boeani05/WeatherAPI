package com.boberlec.weatherApiApp.client;

import com.boberlec.weatherApiApp.exceptions.WeatherClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

// task: weatherapiclient has the task to provide classes (e.g. service) with weather data
@Component
public class WeatherApiClient {

    // load api key from application.properties
    @Value("${weather.api.key}")
    private String apiKey;

    // load apiurl from application.properties
    @Value("${weather.api.base-url}")
    private String apiUrl;

    // load a restclient from config class
    private final RestClient restClient;

    public WeatherApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWeather(String city) {
        try {
            String encodedCity = UriUtils.encodePathSegment(city, StandardCharsets.UTF_8);
            String requestUri = apiUrl + "/" + encodedCity
                    + "?unitGroup=metric&contentType=json&key=" + apiKey;

            Map<String, Object> responseBody = restClient.get()
                    .uri(requestUri)
                    .retrieve()
                    .body(Map.class);

            if (responseBody == null) {
                throw new WeatherClientException("Weather API returned empty response");
            }

            return responseBody;
        } catch (RestClientResponseException e) {
            throw new WeatherClientException("Weather API error: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new WeatherClientException("Can't reach Weather API", e);
        }
    }

}

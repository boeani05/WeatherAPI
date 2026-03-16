package com.boberlec.weatherApiApp.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public Map<String, Object> getWeather(String city) {
        return restClient.get()
                .uri(apiUrl + "/" + city + "?unitGroup=metric" + "&contentType=json" + "&key=" + apiKey)
                .retrieve()
                .body(Map.class);
    }
}

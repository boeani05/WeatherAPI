package com.boberlec.weatherApiApp.client;

import com.boberlec.weatherApiApp.config.DIConfig;
import com.boberlec.weatherApiApp.exceptions.CityNotFoundException;
import com.boberlec.weatherApiApp.exceptions.WeatherClientException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// runs the real client against a small local http server that plays the weather api
class WeatherApiClientTest {

    private final RestClient restClient = new DIConfig().returnRestClient();
    private final List<String> requestedUris = new ArrayList<>();

    private HttpServer weatherApi;
    private int answerStatus = 200;
    private String answerBody = "{\"resolvedAddress\":\"Wien, Österreich\"}";

    @BeforeEach
    void startWeatherApi() throws IOException {
        weatherApi = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        weatherApi.createContext("/", exchange -> {
            requestedUris.add(exchange.getRequestURI().getRawPath() + "?" + exchange.getRequestURI().getRawQuery());

            byte[] body = answerBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(answerStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        weatherApi.start();
    }

    @AfterEach
    void stopWeatherApi() {
        weatherApi.stop(0);
    }

    private WeatherApiClient client() {
        return new WeatherApiClient(restClient, "test-key", "http://127.0.0.1:" + weatherApi.getAddress().getPort() + "/timeline");
    }

    @Test
    void returnsTheAnswerOfTheWeatherApi() {
        Map<String, Object> weather = client().getWeather("Vienna");

        assertThat(weather).containsEntry("resolvedAddress", "Wien, Österreich");
        assertThat(requestedUris).containsExactly("/timeline/Vienna?unitGroup=metric&contentType=json&key=test-key");
    }

    @Test
    void encodesACityWithSpaceOrUmlautExactlyOnce() {
        client().getWeather("New York");
        client().getWeather("Zürich");

        assertThat(requestedUris).containsExactly(
                "/timeline/New%20York?unitGroup=metric&contentType=json&key=test-key",
                "/timeline/Z%C3%BCrich?unitGroup=metric&contentType=json&key=test-key"
        );
    }

    @Test
    void unknownCityBecomesCityNotFound() {
        answerStatus = 400;
        answerBody = "Bad API Request:Invalid location parameter value.";

        assertThatThrownBy(() -> client().getWeather("Xyzzy"))
                .isInstanceOf(CityNotFoundException.class)
                .hasMessageContaining("Xyzzy");
    }

    @Test
    void rejectedApiKeyBecomesWeatherClientException() {
        answerStatus = 401;
        answerBody = "Invalid API key";

        assertThatThrownBy(() -> client().getWeather("Vienna"))
                .isInstanceOf(WeatherClientException.class)
                .hasMessageContaining("401");
    }

    @Test
    void unreachableWeatherApiBecomesWeatherClientException() {
        WeatherApiClient client = client();
        weatherApi.stop(0);

        assertThatThrownBy(() -> client.getWeather("Vienna"))
                .isInstanceOf(WeatherClientException.class)
                .hasMessage("Can't reach Weather API");
    }

    @Test
    void emptyApiKeyStopsTheStartWithAClearMessage() {
        assertThatThrownBy(() -> new WeatherApiClient(restClient, " ", "http://localhost"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("weather.api.key");
    }
}

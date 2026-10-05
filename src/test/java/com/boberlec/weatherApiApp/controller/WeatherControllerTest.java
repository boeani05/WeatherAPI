package com.boberlec.weatherApiApp.controller;

import com.boberlec.weatherApiApp.exceptions.CityNotFoundException;
import com.boberlec.weatherApiApp.exceptions.WeatherClientException;
import com.boberlec.weatherApiApp.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// controller and GlobalExceptionHandler together: which http status does the caller get?
@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void returnsTheWeatherOfTheService() throws Exception {
        when(weatherService.getWeather("Vienna")).thenReturn(Map.of("city", "Wien, Österreich"));

        mockMvc.perform(get("/weather").param("city", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Wien, Österreich"));
    }

    @Test
    void missingCityParameterIsABadRequest() throws Exception {
        mockMvc.perform(get("/weather"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void blankCityIsABadRequest() throws Exception {
        mockMvc.perform(get("/weather").param("city", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City must not be empty."));
    }

    @Test
    void unknownPathIsNotFound() throws Exception {
        mockMvc.perform(get("/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void wrongHttpMethodIsNotAllowed() throws Exception {
        mockMvc.perform(post("/weather").param("city", "Vienna"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    void unknownCityIsNotFound() throws Exception {
        when(weatherService.getWeather("Xyzzy")).thenThrow(new CityNotFoundException("Xyzzy"));

        mockMvc.perform(get("/weather").param("city", "Xyzzy"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No weather data found for city 'Xyzzy'."));
    }

    @Test
    void failingWeatherApiIsABadGateway() throws Exception {
        when(weatherService.getWeather("Vienna")).thenThrow(new WeatherClientException("Can't reach Weather API"));

        mockMvc.perform(get("/weather").param("city", "Vienna"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Weather API Unavailable"));
    }

    @Test
    void unexpectedErrorIsAnInternalServerError() throws Exception {
        when(weatherService.getWeather("Vienna")).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/weather").param("city", "Vienna"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error while processing request"));
    }
}

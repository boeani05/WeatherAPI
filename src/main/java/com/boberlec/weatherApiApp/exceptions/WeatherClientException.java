package com.boberlec.weatherApiApp.exceptions;

public class WeatherClientException extends RuntimeException {

    public WeatherClientException(String message) {
        super(message);
    }

    public WeatherClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

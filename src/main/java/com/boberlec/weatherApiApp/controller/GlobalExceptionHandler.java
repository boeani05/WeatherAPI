package com.boberlec.weatherApiApp.controller;

import com.boberlec.weatherApiApp.exceptions.CityNotFoundException;
import com.boberlec.weatherApiApp.exceptions.WeatherClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Bad Request",
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCityNotFound(CityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Not Found",
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(WeatherClientException.class)
    public ResponseEntity<Map<String, Object>> handleWeatherClientException(WeatherClientException exception) {
        log.warn("Weather API call failed", exception);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Weather API Unavailable",
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception exception) {
        // spring's own exceptions (missing parameter, unknown path, wrong http method) already know
        // their status - answering 500 for them would blame the server for a mistake of the caller
        if (exception instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            String detail = errorResponse.getBody().getDetail();

            return ResponseEntity.status(status).headers(errorResponse.getHeaders()).body(Map.of(
                    "error", status.getReasonPhrase(),
                    "message", detail != null ? detail : status.getReasonPhrase()
            ));
        }

        // everything else is a bug or an outage - without this line the cause would vanish silently
        log.error("Unexpected error while processing request", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", "Unexpected error while processing request"
        ));
    }
}

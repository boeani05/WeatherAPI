package com.boberlec.weatherApiApp.exceptions;

// thrown, if the weather api does not know the requested city
public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String city) {
        super("No weather data found for city '" + city + "'.");
    }
}

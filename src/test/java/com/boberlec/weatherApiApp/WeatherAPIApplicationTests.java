package com.boberlec.weatherApiApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// starts the whole application once: fails, if a bean is missing or wired wrongly.
// key and url are set here, because application.properties is not part of the git repository.
// redis does not have to run for this - the connection is only opened on the first request.
@SpringBootTest(properties = {
        "weather.api.key=test-key",
        "weather.api.base-url=http://localhost/timeline"
})
class WeatherAPIApplicationTests {

    @Test
    void contextLoads() {
    }
}

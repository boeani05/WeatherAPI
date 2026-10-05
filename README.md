# Weather API

A small Spring Boot service that wraps the [Visual Crossing](https://www.visualcrossing.com) weather API and caches the answers in Redis.

Project idea from roadmap.sh: https://roadmap.sh/projects/weather-api-wrapper-service

## What it does

- `GET /weather?city={city}` returns today's weather for a city.
- The first request for a city asks Visual Crossing; the answer is kept in Redis for one hour.
- Further requests for the same city are answered from the cache. `Vienna`, `vienna` and `  VIENNA  ` share one cache entry.
- If Redis is not running, the service still answers, just without caching.

## Requirements

- Java 21 or newer
- Maven
- Docker (for Redis)
- A Visual Crossing API key (free at https://www.visualcrossing.com)

## Getting started

1. Start Redis:

   ```
   docker compose up -d
   ```

2. Create the configuration file from the template:

   ```
   cp application.properties.example src/main/resources/application.properties
   ```

   On Windows without Git Bash: `copy application.properties.example src\main\resources\application.properties`

3. Add your API key. Either write it after `weather.api.key=` in `src/main/resources/application.properties`, or set the environment variable `WEATHER_API_KEY`.

   The file is listed in `.gitignore`, so the key is never committed.

4. Start the application:

   ```
   mvn spring-boot:run
   ```

5. Try it:

   ```
   curl "http://localhost:8080/weather?city=Vienna"
   ```

Without an API key the application stops at startup and says so.

## Response

```json
{
  "city": "Wien, Österreich",
  "description": "Similar temperatures continuing with a chance of rain.",
  "temperature": 12.3,
  "conditions": "Partially cloudy",
  "datetime": "2026-10-05"
}
```

| Field | Meaning |
|---|---|
| `city` | The address Visual Crossing resolved the city to |
| `description` | Outlook for the coming days |
| `temperature` | Mean temperature of today in °C; `null` if the weather API sent none |
| `conditions` | Conditions of today |
| `datetime` | The day the data belongs to |

## Errors

Every error has the same shape:

```json
{
  "error": "Not Found",
  "message": "No weather data found for city 'Xyzzy'."
}
```

| Status | When |
|---|---|
| 400 | `city` is missing or empty |
| 404 | The weather API does not know the city, or the path does not exist |
| 405 | Any method other than `GET` on `/weather` |
| 502 | The weather API is unreachable or rejected the request (for example a wrong API key) |
| 500 | Unexpected error; the cause is written to the log |

## Configuration

All settings live in `src/main/resources/application.properties`.

| Property | Default | Meaning |
|---|---|---|
| `weather.api.key` | `${WEATHER_API_KEY}` | Visual Crossing API key |
| `weather.api.base-url` | Visual Crossing timeline API | Base URL of the weather API |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |
| `spring.data.redis.connect-timeout` | `2s` | How long to wait for a Redis connection |
| `spring.data.redis.timeout` | `2s` | How long to wait for a Redis answer |

To look into the cache:

```
docker exec weather-redis redis-cli keys "weather:*"
docker exec weather-redis redis-cli get "weather:vienna"
```

## Tests

```
mvn test
```

The tests need neither Redis nor an API key.

## Project structure

```
src/main/java/com/boberlec/weatherApiApp
├── WeatherAPIApplication.java     entry point
├── controller
│   ├── WeatherController.java     GET /weather
│   └── GlobalExceptionHandler.java  turns exceptions into error responses
├── service
│   └── WeatherService.java        cache first, weather API on a miss
├── client
│   └── WeatherApiClient.java      calls Visual Crossing
├── cache
│   └── WeatherCache.java          reads and writes Redis
├── config
│   └── DIConfig.java              RestClient and RedisTemplate beans
└── exceptions                     CityNotFoundException, WeatherClientException
```

## Built with

Java 21, Spring Boot 4.1, Spring Data Redis, Redis 7, Maven

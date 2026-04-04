# Breezy Weather — OACP Context

## What this app does
Weather app showing current conditions, forecasts, air quality, and weather alerts for configured locations.

## Capabilities
- check_weather: Returns current weather conditions for a location
- check_forecast: Returns multi-day weather forecast
- open_weather: Opens the weather app UI

## Disambiguation
- "what's the weather/temperature" -> check_weather
- "weather forecast/tomorrow/this week" -> check_forecast
- "open/show weather app" -> open_weather
- If no location specified, uses the default/current location

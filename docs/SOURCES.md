# Weather sources

This is a user-end guide to weather sources available in Breezy Weather. If you are a developer looking to add a new source in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md).

**AccuWeather** is the most complete source, although you may not need so much completeness (not many people cares about ceiling, for example).

**Open-Meteo** is the only free and open source weather source on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, however lacks major features (alerts, realtime precipitations and reverse geocoding).


## Accuracy

When deciding about which source you want to use, accuracy of data is probably the most important criteria.

Here are some suggestions based on various criteria or testimonies (you can suggest others in GitHub discussions):

| Country/Continent | Main source  | Air quality | Pollen      | Minutely     | Alerts       |
| ----------------- | ------------ | ----------- | ----------- | ------------ | ------------ |
| North America     |              |             | AccuWeather |              |              |
| Europe            |              |             | Open-Meteo  |              |              |
| United States     | [Forecast Advisor](https://forecastadvisor.com/) | | |  |              |
| France            | Météo-France |             | Open-Meteo  | Météo-France | Météo-France |
| Norway            | Météo-France |             | Open-Meteo  | Météo-France | Météo-France |

Note that secondary sources are coming in next version (v4.5.0-beta) and are not available yet. In the meantime, you can add the same city with two different sources.


## Status

| Sources       | Open-Meteo | AccuWeather | MET Norway | OpenWeather   | Pirate Weather | Météo-France | China³ |
|---------------|------------|-------------|------------|---------------|----------------|--------------|--------|
| **API key**   | None       | Optional    | None       | Rate-limited¹ | Rate-limited¹  | Optional     | None   |
| **Countries** | Worldwide² | Worldwide²  | Worldwide² | Worldwide²    | Worldwide²     | Worldwide²   | China  |

Additional sources are available to configure but requires an API key. You can find them at the bottom of this page.

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features may not be available everywhere.
* ³ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC


## Main features

| Sources                         | Open-Meteo | AccuWeather   | MET Norway         | OpenWeather | Pirate Weather | Météo-France  | China   |
|---------------------------------|------------|---------------|--------------------|-------------|----------------|---------------|---------|
| **Daily (days)**                | 15         | 15            | ~10                | 7 or 8      | 8              | 14            | 15      |
| **Hourly (days)**               | 16         | 10            | ~10                | 2           | 2              | 15            | 1       |
| **Weather**                     | ✅          | ✅             | *Text in progress* | ✅           | ✅              | ✅             | ✅       |
| **Temperature**                 | ✅          | ✅             | ✅                  | ✅           | ✅              | ✅             | ✅       |
| **Precipitation**               | ✅          | ✅ (RSI)       | ✅                  | ✅ (RS)      | ✅ (RS)         | ✅ (RS)        | ❌       |
| **Precipitation probability**   | ✅          | ✅ (TRSI)      | ✅ (T)              | ✅           | ✅              | ✅ (RSI)       | Daily   |
| **Wind**                        | ✅          | ✅             | ✅                  | ✅           | ✅              | ✅             | ✅       |
| **Air quality**                 | ✅          | ✅             | Norway             | ✅           | ❌              | France (AURA) | Current |
| **Allergens**                   | Europe     | North America | ❌                  | ❌           | ❌              | ❌             | ❌       |
| **UV**                          | ✅          | ✅             | ✅                  | ✅           | ✅              | ✅             | ❌       |
| **Precipitations in next hour** | ❌          | ✅             | Nordic area        | ✅           | ✅              | France        | ✅       |
| **Sun & Moon & Moon phase**     | ✅          | ✅             | ✅                  | ✅           | ✅              | ✅             | ✅       |
| **Alerts**                      | ❌          | ✅             | *In progress*      | ✅¹          | ✅              | ✅             | ✅       |

* ¹ List of available countries: https://openweathermap.org/api/push-weather-alerts#listsource


Legend:

| Letter | Meaning      |
|--------|--------------|
| R      | Rain         |
| T      | Thunderstorm |
| S      | Snow         |
| I      | Ice          |

Ultimate goal of the app would be to modularize as to have a main weather source, and then being able to complete “precipitations in next hour” and “alerts” from one or more sources.


## Other weather data

| Sources                    | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Pirate Weather | Météo-France | China   |
|----------------------------|------------|-------------|------------|-------------|----------------|--------------|---------|
| **Humidity**               | ✅          | ✅           | ✅          | ✅           | ✅              | ✅            | Current |
| **Dew point**              | ✅          | ✅           | ✅          | ✅           | ✅              | ✅            | Current |
| **Pressure**               | ✅          | Current     | ✅          | ✅           | ✅              | ✅            | ❌       |
| **Cloud cover**            | ✅          | ✅           | ❌          | ✅           | ✅              | ✅            | ❌       |
| **Visibility**             | ✅          | ✅           | ❌          | ✅           | Current        | ❌            | Current |
| **Ceiling**                | ❌          | ✅           | ❌          | ❌           | ❌              | ❌            | ❌       |
| **Precipitation duration** | ❌          | ✅ (RSI)     | ❌          | ❌           | ❌              | ❌            | ❌       |
| **Hours of sun**           | ✅          | ✅           | ✅          | ✅           | ✅              | ✅            | ✅       |
| **Yesterday temperature**  | ✅          | ✅           | ❌          | ❌           | ❌              | ❌            | ✅       |


## Location

| Sources               | Open-Meteo | AccuWeather | MET Norway     | OpenWeather    | Pirate Weather | Météo-France   | China |
|-----------------------|------------|-------------|----------------|----------------|----------------|----------------|-------|
| **Search**            | ✅          | ✅           | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅¹    |
| **Reverse geocoding** | ❌²         | ✅           | ❌²             | ❌²             | ❌²             | ✅²             | ✅¹    |

* ¹ TimeZone is assumed to be China
* ² TimeZone is assumed to be the same as device


# Additional sources with mandatory API key

| Sources                         | HERE     |
|---------------------------------|----------|
| **Daily (days)**                | 6        |
| **Hourly (days)**               | 6        |
| **Weather**                     | ✅        |
| **Temperature**                 | ✅        |
| **Precipitation**               | ✅        |
| **Precipitation probability**   | ✅        |
| **Wind**                        | ✅        |
| **Air quality**                 | ❌        |
| **Allergens**                   | ❌        |
| **UV**                          | ✅        |
| **Precipitations in next hour** | ❌        |
| **Sun & Moon & Moon phase**     | ✅        |
| **Alerts**                      | US (NWS) |
| **Humidity**                    | ✅        |
| **Dew point**                   | ✅        |
| **Pressure**                    | ✅        |
| **Cloud cover**                 | ❌        |
| **Visibility**                  | ✅        |
| **Ceiling**                     | ❌        |
| **Precipitation duration**      | ❌        |
| **Hours of sun**                | ✅        |
| **Yesterday temperature**       | ❌        |
| **Search**                      | ✅        |
| **Reverse geocoding**           | ✅        |

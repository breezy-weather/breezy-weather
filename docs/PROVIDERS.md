# Weather providers

This is a user-end guide to weather providers available in Breezy Weather. If you are a developer looking to add a new provider in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md).

**AccuWeather** is the most complete provider, although you may not need so much completeness (not many people cares about ceiling, for example).

**Open-Meteo** is the only free and open source provider on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, however lacks major features (alerts, realtime precipitations and reverse geocoding). Pollen is available and remains to be implemented.

When deciding about which provider you want to use, accuracy of data is probably the most important criteria, however only you can know which one is the best for your country.


## Status

| Providers     | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | China⁵ |
|---------------|------------|-------------|------------|-------------|--------------|--------|
| **API key**   | None       | Optional    | None       | Optional¹   | Optional     | None   |
| **Countries** | Worldwide² | Worldwide²  | Worldwide³ | Worldwide²  | Worldwide⁴   | China  |

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features not available everywhere.
* ³ Some features only available in Norway, or Nordic area.
* ⁴ Some features only available for France, overseas (DROM-COM) included. Air quality restricted to Auvergne-Rhône-Alpes.
* ⁵ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC
* ⁶ Except daily cloud cover


## Availability of main features

| Providers                       | Open-Meteo    | AccuWeather | MET Norway    | OpenWeather | Météo-France | China           |
|---------------------------------|---------------|-------------|---------------|-------------|--------------|-----------------|
| **Weather/temperature**         | ✅             | ✅           | ✅             | ✅           | ✅            | ✅               |
| **Precipitation**               | ✅             | ✅           | ✅             | ✅           | ✅            | Daily (partial) | 
| **Wind**                        | ✅             | ✅           | ✅             | ✅           | ✅            | ✅               |
| **Air quality**                 | ✅             | ✅           | ✅             | ✅           | ✅            | Current         |
| **Pollen**                      | *In progress* | Daily       | ❌             | ❌           | ❌            | ❌               |
| **UV**                          | ✅             | ✅           | ✅             | ✅           | ✅            | ❌               |
| **Precipitations in next hour** | ❌             | ✅           | ✅             | ✅           | ✅            | ✅               |
| **Sun, Moon & Moon phase**      | Sun           | ✅           | ✅             | Sun, Moon   | ✅            | Sun             |
| **Alerts**                      | ❌             | ✅           | *In progress* | ✅           | ✅            | ✅               |

Some features may not be available in some countries.

Ultimate goal of the app would be to modularize as to have a main weather provider, and then being able to complete “precipitations in next hour” and “alerts” from one or more providers.


## Detailed available data

### Location providers

| Providers             | Open-Meteo | AccuWeather | MET Norway     | OpenWeather    | Météo-France   | China |
|-----------------------|------------|-------------|----------------|----------------|----------------|-------|
| **Search**            | ✅          | ✅           | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅¹    |
| **Reverse geocoding** | ❌          | ✅           | ❌              | ❌              | ✅²             | ✅¹    |

* ¹ TimeZone is assumed to be China
* ² TimeZone is assumed to be the same as device


### Daily forecast

| Providers                     | Open-Meteo    | AccuWeather | MET Norway | OpenWeather | Météo-France | China |
|-------------------------------|---------------|-------------|------------|-------------|--------------|-------|
| **Days**                      | 16            | 15          | ~10        | 7 or 8      | 15           | 15    |
| **Weather**                   | ✅³            | ✅           | Partial³⁵  | ✅⁴          | ✅⁴           | ✅     |
| **Temperature**               | ✅             | ✅           | ✅¹         | ✅           | ✅            | ✅     |
| **Precipitation**             | ✅¹            | ✅ (RSI)     | ✅¹         | ✅¹ (RS)     | ✅¹ (RS)      | ❌     |
| **Precipitation probability** | ✅¹            | ✅ (TRSI)    | ✅¹ (T)     | ✅¹          | ✅¹ (RSI)     | ✅     |
| **Precipitation duration**    | ❌             | ✅ (RSI)     | ❌          | ❌           | ❌            | ❌     |
| **Wind**                      | ✅¹            | ✅           | ✅¹         | ✅¹          | ✅¹           | ✅     |
| **Cloud cover**               | ✅¹            | ✅           | ❌          | ✅¹          | ✅¹           | ❌     |
| **Sunset/sunrise**            | ✅             | ✅           | ✅⁶         | ✅           | ✅            | ✅     |
| **Moonset/moonrise**          | ❌             | ✅           | ✅⁶         | ✅           | ✅⁶           | ❌     |
| **Moon phase**                | ❌             | ✅           | ✅⁶         | ❌           | ✅⁶           | ❌     |
| **Air quality**               | ✅¹            | ✅¹          | ✅¹         | ✅¹          | ✅¹           | ❌     |
| **Pollen**                    | *In progress* | ✅           | ❌          | ❌           | ❌            | ❌     |
| **UV**                        | ✅             | ✅           | ✅¹         | ✅           | ✅            | ❌     |
| **Hours of sun**              | ✅²            | ✅           | ✅²         | ✅²          | ✅²           | ✅²    |

* ¹ Extrapolated from hourly forecast
* ² Extrapolated from sunrise/sunset
* ³ Extrapolated from hourly forecast at 12:00 for day, 00:00 for night
* ⁴ Same weather for day and night
* ⁵ Missing text
* ⁶ Only available for the current day

Legend:

| Letter | Meaning      |
|--------|--------------|
| R      | Rain         |
| T      | Thunderstorm |
| S      | Snow         |
| I      | Ice          |


### Hourly forecast

| Providers                     | Open-Meteo    | AccuWeather | MET Norway | OpenWeather | Météo-France | China |
|-------------------------------|---------------|-------------|------------|-------------|--------------|-------|
| **Days**                      | 16            | 5           | ~10¹       | 2           | 15²          | 1     |
| **Weather**                   | ✅             | ✅           | Partial³   | ✅           | ✅            | ✅     |
| **Temperature**               | ✅             | ✅           | ✅          | ✅           | ✅            | ✅     |
| **Precipitation**             | ✅             | ✅ (RSI)     | ✅          | ✅ (RS)      | ✅ (RS)       | ❌     |
| **Precipitation probability** | ✅             | ✅ (TRSI)    | ✅ (T)      | ✅           | ✅ (RSI)      | ❌     |
| **Wind**                      | ✅             | ✅           | ✅          | ✅           | ✅            | ✅     |
| **Air quality**               | ✅             | ✅           | ✅          | ✅           | ✅            | ❌     |
| **Pollen**                    | *In progress* | ❌           | ❌          | ❌           | ❌            | ❌     |
| **UV**                        | ✅             | ✅           | ✅          | ✅           | ✅⁴           | ❌     |

Not yet displayed in app:

| Providers       | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | China |
|-----------------|------------|-------------|------------|-------------|--------------|-------|
| **Humidity**    | ✅          | ✅           | ✅          | ✅           | ✅            | ❌     |
| **Dew point**   | ✅          | ✅           | ✅          | ✅           | ❌            | ❌     |
| **Pressure**    | ✅          | ❌           | ✅          | ✅           | ✅            | ❌     |
| **Cloud cover** | ✅          | ✅           | ❌          | ✅           | ✅            | ❌     |
| **Visibility**  | ✅          | ✅           | ❌          | ✅           | ❌            | ❌     |

* ¹ Every 6 hours after 3~4 days
* ² Every 3 hours after 2 days, every 6 hours after 4 days
* ³ Missing text
* ⁴ Extrapolated from max UV of the day

Legend:

| Letter | Meaning      |
|--------|--------------|
| R      | Rain         |
| T      | Thunderstorm |
| S      | Snow         |
| I      | Ice          |


### Current weather

| Providers       | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | China |
|-----------------|------------|-------------|------------|-------------|--------------|-------|
| **Weather**     | ✅          | ✅           | Partial²³  | ✅           | ✅¹           | ✅     |
| **Temperature** | ✅¹         | ✅           | ✅²         | ✅           | ✅¹           | ✅     |
| **Wind**        | ✅          | ✅           | ✅²         | ✅           | ✅¹           | ✅     |
| **UV**          | ✅⁵         | ✅           | ✅²         | ✅           | ✅⁵           | ✅     |
| **Air quality** | ✅¹         | ✅¹          | ✅¹         | ✅           | ✅⁴           | ✅     |
| **Humidity**    | ✅¹         | ✅           | ✅²         | ✅           | ✅¹           | ✅     |
| **Dew point**   | ✅¹         | ✅           | ✅¹         | ✅           | ❌            | ❌     |
| **Pressure**    | ✅¹         | ✅           | ✅¹         | ✅           | ✅¹           | ✅     |
| **Cloud cover** | ✅¹         | ✅           | ❌          | ✅           | ✅¹           | ❌     |
| **Visibility**  | ✅¹         | ✅           | ❌          | ✅           | ❌            | ✅     |
| **Ceiling**     | ❌          | ✅           | ❌          | ❌           | ❌            | ❌     |

*In progress* means data is available in the API (or can be extrapolated) and can be implemented.

* ¹ Extrapolated from hourly forecast
* ² Extrapolated from hourly forecast for countries outside Nordic area
* ³ Missing text
* ⁴ Only in Auvergne-Rhône-Alpes
* ⁵ Extrapolated from max UV of the day


### Other data

| Providers                           | Open-Meteo | AccuWeather | MET Norway    | OpenWeather | Météo-France | China |
|-------------------------------------|------------|-------------|---------------|-------------|--------------|-------|
| **Precipitations in next hour**     | ❌          | ✅           | ✅³            | ✅           | ✅²           | ✅     |
| **Alerts**                          | ❌          | ✅           | *In progress* | ✅¹          | ✅            | ✅     |
| **Yesterday daytime temperature**   | ✅          | ✅           | ❌             | ❌           | ❌            | ✅     |
| **Yesterday nighttime temperature** | ✅          | ✅           | ❌             | ❌           | ❌            | ✅     |

* ¹ List of available countries: https://openweathermap.org/api/push-weather-alerts#listsource
* ² Only for France, and only for some cities. Rain intensity is estimated.
* ³ Only for Nordic Area
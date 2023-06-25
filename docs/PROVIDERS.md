# Weather providers

This is a user-end guide to weather providers available in Breezy Weather. If you are a developer looking to add a new provider in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md).


**AccuWeather** is the most complete provider.

**Open-Meteo** is the only free and open source provider on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, however lacks major features (reverse geocoding, alerts and realtime precipitations). Air Quality and Pollen are available and remains to be implemented with appropriate credits and acknowledgement.

## Status

| Providers | Open-Meteo  | AccuWeather | MET Norway  | OpenWeather | Météo-France | China⁵ |
|-----------|-------------|-------------|-------------|-------------|--------------|--------|
| Status    | In progress | As is       | In progress | As is       | In progress  | As is  |
| API key   | None        | Optional    | None        | Optional¹   | Optional     | None   |
| Countries | Worldwide²  | Worldwide²  | Worldwide³  | Worldwide²  | Worldwide⁴   | China  |

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features not available everywhere.
* ³ Some features only available in Norway, or Nordic area.
* ⁴ Some features only available for France, overseas (DROM-COM) included. Air quality restricted to Auvergne-Rhône-Alpes.
* ⁵ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC

## Availability of main features

| Providers                   | Open-Meteo    | AccuWeather | MET Norway    | OpenWeather | Météo-France | China   |
|-----------------------------|---------------|-------------|---------------|-------------|--------------|---------|
| Daily weather/temperature   | ✅             | ✅           | ✅             | ✅           | ✅            | ✅       |
| Daily precipitation         | *In progress* | ✅           | ✅             | ✅           | ✅            | ❌       | 
| Daily wind                  | ✅             | ✅           | ✅             | ✅           | ✅            | ✅       |
| Daily air quality           | *In progress* | ✅           | *In progress* | ✅           | ✅            | ❌       |
| Daily UV                    | ✅             | ✅           | ✅             | ✅           | ✅            | ❌       |
| Hourly weather/temperature  | ✅             | ✅           | ✅             | ✅           | ✅            | ✅       |
| Hourly precipitation        | *In progress* | ✅           | ✅             | ✅           | ✅            | Partial |
| Hourly wind                 | ✅             | ✅           | ✅             | ✅           | ✅            | ✅       |
| Hourly air quality          | *In progress* | ✅           | *In progress* | ✅           | ✅            | ❌       |
| Hourly UV                   | ✅             | ✅           | ✅             | ✅           | ✅            | ❌       |
| Precipitations in next hour | ❌             | ✅           | *In progress* | ✅           | ✅            | ❌       |
| Current air quality         | *In progress* | ✅           | *In progress* | ✅           | ✅            | ✅       |
| Daily pollen                | *In progress* | ✅           | ❌             | ❌           | ❌            | ❌       |
| Sun, Moon & Moon phase      | Sun           | ✅           | ✅             | Sun, Moon   | ✅            | Sun     |
| Alerts                      | ❌             | ✅           | *In progress* | ✅           | ✅            | ✅       |

Some features may not be available in some countries.

Ultimate goal of the app would be to modularize as to have a main weather provider, and then being able to complete “precipitations in next hour” and “alerts” from one or more providers.

## Detailed available data

### Location providers

| Providers         | Open-Meteo | AccuWeather | MET Norway     | OpenWeather    | Météo-France   | China    |
|-------------------|------------|-------------|----------------|----------------|----------------|----------|
| Search            | ✅          | ✅           | ✅ (Open-Meteo) | ✅ (Open-Meteo) | ✅ (Open-Meteo) | Partial¹ |
| Reverse geocoding | ❌          | ✅           | ❌              | ❌              | ✅²             | ❌        |

* ¹ Currently based on a hardcoded list of cities bundled with the app.
* ² TimeZone is assumed to be the same as device


### Daily forecast

| Providers                 | Open-Meteo    | AccuWeather | MET Norway    | OpenWeather   | Météo-France  | China |
|---------------------------|---------------|-------------|---------------|---------------|---------------|-------|
| Days                      | 16            | 15          | ~10           | 7 or 8        | 15            | 15    |
| Weather                   | ✅³            | ✅           | Partial³⁵     | ✅⁴            | ✅⁴            | ✅     |
| Temperature               | ✅             | ✅           | ✅¹            | ✅             | ✅             | ✅     |
| Precipitation             | *In progress* | ✅ (RSI)     | ✅¹            | ✅¹ (RS)       | ✅¹ (RS)       | ❌     |
| Precipitation probability | ✅¹            | ✅ (TRSI)    | ✅¹ (T)        | ✅¹            | ✅¹ (RSI)      | ✅     |
| Precipitation duration    | ❌             | ✅ (RSI)     | ❌             | ❌             | ❌             | ❌     |
| Wind                      | ✅¹            | ✅           | ✅¹            | ✅¹            | ✅¹            | ✅     |
| Cloud cover               | *In progress* | ✅           | ❌             | *In progress* | *In progress* | ❌     |
| Sunset/sunrise            | ✅             | ✅           | ✅             | ✅             | ✅             | ✅     |
| Moonset/moonrise          | ❌             | ✅           | ✅             | ✅             | ✅⁶            | ❌     |
| Moon phase                | ❌             | ✅           | ✅             | ❌             | ✅⁶            | ❌     |
| Air quality               | *In progress* | ✅¹          | *In progress* | ✅¹            | ✅¹            | ❌     |
| Pollen                    | *In progress* | ✅           | ❌             | ❌             | ❌             | ❌     |
| UV                        | ✅             | ✅           | ✅¹            | ✅             | ✅             | ❌     |
| Hours of sun              | ✅²            | ✅           | ✅²            | ✅²            | ✅²            | ✅²    |

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

| Providers                 | Open-Meteo    | AccuWeather | MET Norway    | OpenWeather | Météo-France | China |
|---------------------------|---------------|-------------|---------------|-------------|--------------|-------|
| Days                      | 16            | 5           | ~10¹          | 2           | 15²          | 1     |
| Weather                   | ✅             | ✅           | Partial³      | ✅           | ✅            | ✅     |
| Temperature               | ✅             | ✅           | ✅             | ✅           | ✅            | ✅     |
| Precipitation             | *In progress* | ✅ (RSI)     | ✅             | ✅ (RS)      | ✅ (RS)       | ❌     |
| Precipitation probability | ✅             | ✅ (TRSI)    | ✅ (T)         | ✅           | ✅ (RSI)      | ❌     |
| Wind                      | ✅             | ✅           | ✅             | ✅           | ✅            | ✅     |
| Air quality               | *In progress* | ✅           | *In progress* | ✅           | ✅            | ❌     |
| Pollen                    | *In progress* | ❌           | ❌             | ❌           | ❌            | ❌     |
| UV                        | *In progress* | ✅           | ✅             | ✅           | ✅⁴           | ❌     |

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

| Providers   | Open-Meteo    | AccuWeather | MET Norway    | OpenWeather | Météo-France | China |
|-------------|---------------|-------------|---------------|-------------|--------------|-------|
| Weather     | ✅             | ✅           | Partial²³     | ✅           | ✅¹           | ✅     |
| Temperature | ✅¹            | ✅           | ✅²            | ✅           | ✅¹           | ✅     |
| Wind        | ✅             | ✅           | ✅²            | ✅           | ✅¹           | ✅     |
| UV          | ✅⁵            | ✅           | ✅²            | ✅           | ✅⁵           | ✅     |
| Air quality | *In progress* | ✅¹          | *In progress* | ✅           | ✅⁴           | ✅     |
| Humidity    | ✅¹            | ✅           | ✅²            | ✅           | ❌            | ✅     |
| Pressure    | ✅¹            | ✅           | ✅¹            | ✅           | ❌            | ✅     |
| Visibility  | ✅¹            | ✅           | ❌             | ✅           | ❌            | ✅     |
| Dew point   | ✅¹            | ✅           | ✅²            | ✅           | ❌            | ❌     |
| Cloud cover | ✅¹            | ✅           | ❌             | ✅           | ❌            | ❌     |
| Ceiling     | ❌             | ✅           | ❌             | ❌           | ❌            | ❌     |

*In progress* means data is available in the API (or can be extrapolated) and can be implemented.

* ¹ Extrapolated from hourly forecast
* ² Extrapolated from hourly forecast, current data is available in the Nowcast API for Nordic area and can be implemented
* ³ Missing text
* ⁴ Only in Auvergne-Rhône-Alpes
* ⁵ Extrapolated from max UV of the day


### Other data

| Providers                       | Open-Meteo | AccuWeather | MET Norway    | OpenWeather | Météo-France | China |
|---------------------------------|------------|-------------|---------------|-------------|--------------|-------|
| Precipitations in next hour     | ❌          | ✅           | *In progress* | ✅           | ✅²           | ❌     |
| Alerts                          | ❌          | ✅           | *In progress* | ✅¹          | ✅            | ✅     |
| Yesterday daytime temperature   | ✅          | ✅           | ❌             | ❌           | ❌            | ✅     |
| Yesterday nighttime temperature | ✅          | ✅           | ❌             | ❌           | ❌            | ✅     |

* ¹ List of available countries: https://openweathermap.org/api/push-weather-alerts#listsource
* ² Only for France, and only for some cities. Rain intensity is estimated.

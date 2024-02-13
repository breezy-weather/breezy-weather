# Weather sources

This is a user-end guide to weather sources available in Breezy Weather. If you are a developer looking to add a new source in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md).

**Open-Meteo** is the only free and open source weather source on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, and probably more accurate as well, however still lacks a few features (station observations, alerts, reverse geocoding), but you can combine the missing features with a secondary source (see below).


## Suggestions by country

When deciding about which source you want to use, accuracy of data is probably the most important criteria.

When possible, we suggest using data from your national weather source, which are usually best tailored for your country. Additional, most of the time, you will benefit from a large network of observation stations for real-time weather (instead of having “current weather” extrapolated from forecast models refreshed only every few hours).

When not possible or for some data not usually provided by national weather sources (such as Air quality, Pollen), we suggest sources that make use of Open-Data for specific-country or continent (such as Copernicus for Europe air quality, used by Open-Meteo).

Based on these criteria, here are some suggestions:

### North America

| Country       | Main source                                      | Air quality | Pollen      | Minutely | Alerts | Normals |
|---------------|--------------------------------------------------|-------------|-------------|----------|--------|---------|
| Canada        | ECCC                                             | AccuWeather | AccuWeather |          | ECCC   | ECCC    |
| United States | [Forecast Advisor](https://forecastadvisor.com/) | AccuWeather | AccuWeather |          | NWS    |         |


### South America

*No recommendation yet.*


### Europe

| Country         | Main source               | Air quality | Pollen     | Minutely     | Alerts       | Normals      |
|-----------------|---------------------------|-------------|------------|--------------|--------------|--------------|
| Danmark         | DMI or MET Norway         | Open-Meteo  | Open-Meteo | MET Norway   |              |              |
| Deutschland     | Bright Sky (DWD)          | Open-Meteo  | Open-Meteo |              | Bright Sky   |              |
| France          | Météo-France              |             | Open-Meteo | Météo-France | Météo-France | Météo-France |
| Ireland         | MET Éireann               | Open-Meteo  | Open-Meteo |              |              |              |
| Norge/Noreg     | MET Norway                | MET Norway  | Open-Meteo | MET Norway   |              |              |
| Sverige         | SMHI or MET Norway        | Open-Meteo  | Open-Meteo | MET Norway   |              |              |
| Other countries | Open-Meteo¹ or MET Norway |             | Open-Meteo |              |              |              |

¹ Has forecast data from DWD (Deutschland), Météo-France, MET Norway, AM ARPAE ARPAP (Italia) and ECWMF (Europe) weather models


### Asia

| Country | Main source       | Air quality | Pollen | Minutely | Alerts | Normals |
|---------|-------------------|-------------|--------|----------|--------|---------|
| 中国      | 中国 or Open-Meteo¹ | 中国          |        | 中国       | 中国     |         |
| 日本      | Open-Meteo¹       |             |        |          |        |         |

¹ Has forecast data from CMA (China) and JMA (Japan)


### Oceania

| Country   | Main source | Air quality | Pollen | Minutely | Alerts | Normals |
|-----------|-------------|-------------|--------|----------|--------|---------|
| Australia | Open-Meteo¹ |             |        |          |        |         |

¹ Has forecast data from Australian Bureau of Meteorology (BOM)


## Status

| Sources       | Open-Meteo | AccuWeather | MET Norway | OpenWeather   | Pirate Weather | Météo-France | DMI       |
|---------------|------------|-------------|------------|---------------|----------------|--------------|-----------|
| **API key**   | None       | Optional    | None       | Rate-limited¹ | Rate-limited¹  | Optional     | None      |
| **Countries** | Worldwide² | Worldwide²  | Worldwide² | Worldwide²    | Worldwide²     | Worldwide²   | Worldwide |

| Sources       | China³ | NWS  | Bright Sky | ECCC   | SMHI   | MET Éireann |
|---------------|--------|------|------------|--------|--------|-------------|
| **API key**   | None   | None | None       | None   | None   | None        |
| **Countries** | China  | USA  | Germany    | Canada | Sweden | Ireland     |

Additional sources are available to configure but requires an API key. You can find them at the bottom of this page.

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features may not be available everywhere.
* ³ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC


## Main features supported by each source

| Sources                       | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI |
|-------------------------------|------------|-------------|------------|-------------|--------------|-----|
| **Daily (days)**              | 15         | 15          | ~10        | 7 or 8      | 14           | 10  |
| **Hourly (days)**             | 16         | 10          | ~10        | 2           | 15           | 10  |
| **Weather**                   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |
| **Temperature**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |
| **Precipitation**             | ✅          | ✅ (RSI)     | ✅          | ✅ (RS)      | ✅ (RS)       | ✅   |
| **Precipitation probability** | ✅          | ✅ (TRSI)    | ✅ (T)      | ✅           | ✅ (RSI)      | ❌   |
| **Wind**                      | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |
| **UV**                        | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   |
| **Sun & Moon & Moon phase**   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |

| Sources                       | China | NWS    | Bright Sky | ECCC | SMHI | MET Éireann |
|-------------------------------|-------|--------|------------|------|------|-------------|
| **Daily (days)**              | 15    | 7      | 10         | 6    | 15   | 7           |
| **Hourly (days)**             | 1     | 7      | 10         | 1    | 15   | 7           |
| **Weather**                   | ✅     | ✅      | ✅          | ✅    | ✅    | ✅           |
| **Temperature**               | ✅     | ✅      | ✅          | ✅    | ✅    | ✅           |
| **Precipitation**             | ❌     | ✅ (SI) | ✅          | ❌    | ✅    | ✅           |
| **Precipitation probability** | Daily | ✅ (T)  | ✅          | ✅    | T    | ❌           |
| **Wind**                      | ✅     | ✅      | ✅          | ✅    | ✅    | ✅           |
| **UV**                        | ❌     | ❌      | ❌          | ❌    | ❌    | ❌           |
| **Sun & Moon & Moon phase**   | ✅     | ✅      | ✅          | ✅    | ✅    | ✅           |

Note that no forecast above 7 days is reliable, so you should not decide based on the highest number of days available.


## Features that can be added from other sources

The following features, if not available from your selected source, can be added from another source.

| Sources                            | Open-Meteo | AccuWeather   | MET Norway    | OpenWeather | Météo-France | DMI     |
|------------------------------------|------------|---------------|---------------|-------------|--------------|---------|
| **Air quality**                    | ✅          | ✅             | Norway        | ✅           | ❌            | ❌       |
| **Pollen**                         | Europe     | North America | ❌             | ❌           | ❌            | ❌       |
| **Precipitation in the next hour** | ✅²         | ✅             | Nordic area   | ✅           | France       | ❌       |
| **Alerts**                         | ❌          | ✅             | *In progress* | ✅¹          | ✅            | Denmark |
| **Normals**                        | Average    | ✅             | Average       | Average     | ✅            | Average |

| Sources                            | China   | NWS     | Bright Sky | ECCC | SMHI    | MET Éireann   |
|------------------------------------|---------|---------|------------|------|---------|---------------|
| **Air quality**                    | Current | ❌       | ❌          | ❌    | ❌       | ❌             |
| **Pollen**                         | ❌       | ❌       | ❌          | ❌    | ❌       | ❌             |
| **Precipitation in the next hour** | ✅       | ❌       | ❌          | ❌    | ❌       | ❌             |
| **Alerts**                         | ✅       | ✅       | ✅          | ✅    | ❌       | *In progress* |
| **Normals**                        | Average | Average | Average    | ✅    | Average | Average       |

* ¹ List of available countries: https://openweathermap.org/api/push-weather-alerts#listsource
* ² Works best in Europe at the moment

Legend:

| Letter | Meaning      |
|--------|--------------|
| R      | Rain         |
| T      | Thunderstorm |
| S      | Snow         |
| I      | Ice          |


## Other weather data

| Sources                    | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI |
|----------------------------|------------|-------------|------------|-------------|--------------|-----|
| **Humidity**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |
| **Dew point**              | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   |
| **Pressure**               | ✅          | Current     | ✅          | ✅           | ✅            | ✅   |
| **Cloud cover**            | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   |
| **Visibility**             | ✅          | ✅           | ❌          | ✅           | ❌            | ✅   |
| **Ceiling**                | ❌          | ✅           | ❌          | ❌           | ❌            | ❌   |
| **Precipitation duration** | ❌          | ✅ (RSI)     | ❌          | ❌           | ❌            | ❌   |
| **Hours of sun**           | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   |

| Sources                    | China   | NWS | Bright Sky | ECCC    | SMHI | MET Éireann |
|----------------------------|---------|-----|------------|---------|------|-------------|
| **Humidity**               | Current | ✅   | ✅          | Current | ✅    | ✅           |
| **Dew point**              | Current | ✅   | ✅          | Current | ✅    | ✅           |
| **Pressure**               | ❌       | ✅   | ✅          | Current | ✅    | ✅           |
| **Cloud cover**            | ❌       | ✅   | ✅          | ❌       | ❌    | ❌           |
| **Visibility**             | Current | ✅   | ✅          | Current | ✅    | ❌           |
| **Ceiling**                | ❌       | ❌   | ❌          | ❌       | ❌    | ❌           |
| **Precipitation duration** | ❌       | ❌   | ❌          | ❌       | ❌    | ❌           |
| **Hours of sun**           | ✅       | ❌   | ✅          | ✅       | ❌    | ❌           |

¹ Median from daily forecast


## Location

| Sources               | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI     |
|-----------------------|------------|-------------|------------|-------------|--------------|---------|
| **Search**            | ✅          | ✅           | Default    | Default     | Default      | Default |
| **Reverse geocoding** | ❌²         | ✅           | ❌²         | ❌²          | ✅²           | ✅       |

| Sources               | China | NWS     | Bright Sky | ECCC    | SMHI    | MET Éireann |
|-----------------------|-------|---------|------------|---------|---------|-------------|
| **Search**            | ✅³    | Default | Default    | Default | Default | Default     |
| **Reverse geocoding** | ✅³    | ✅       | ❌²         | ✅²      | ❌²      | ❌²          |

* ¹ Default means it will use the configured location search source in settings. By default, it is Open-Meteo.
* ² TimeZone is assumed to be the same as device
* ³ TimeZone is assumed to be China


# Additional sources with mandatory API key

## Main features

| Sources                         | Pirate Weather | HERE     |
|---------------------------------|----------------|----------|
| **Daily (days)**                | 8              | 6        |
| **Hourly (days)**               | 2              | 6        |
| **Weather**                     | ✅              | ✅        |
| **Temperature**                 | ✅              | ✅        |
| **Precipitation**               | ✅ (RS)         | ✅        |
| **Precipitation probability**   | ✅              | ✅        |
| **Wind**                        | ✅              | ✅        |
| **UV**                          | ✅              | ✅        |
| **Sun & Moon & Moon phase**     | ✅              | ✅        |

## Features that can be added from other sources

| Sources                            | Pirate Weather | HERE     |
|------------------------------------|----------------|----------|
| **Air quality**                    | ❌              | ❌        |
| **Pollen**                         | ❌              | ❌        |
| **Precipitation in the next hour** | ✅              | ❌        |
| **Alerts**                         | ✅              | ❌        |
| **Normals**                        | Average¹       | Average¹ |

¹ Median from daily forecast

## Other weather data

| Sources                         | Pirate Weather | HERE     |
|---------------------------------|----------------|----------|
| **Humidity**                    | ✅              | ✅        |
| **Dew point**                   | ✅              | ✅        |
| **Pressure**                    | ✅              | ✅        |
| **Cloud cover**                 | ✅              | ❌        |
| **Visibility**                  | Current        | ✅        |
| **Ceiling**                     | ❌              | ❌        |
| **Precipitation duration**      | ❌              | ❌        |
| **Hours of sun**                | ✅              | ✅        |
| **Search**                      | Default        | ✅        |
| **Reverse geocoding**           | ❌¹             | ✅        |

* ¹ TimeZone is assumed to be the same as device


# Combinable sources

| Sources                            | Open-Meteo | AccuWeather | MET Norway  | OpenWeather | Pirate Weather | Météo-France | DMI           |
|------------------------------------|------------|-------------|-------------|-------------|----------------|--------------|---------------|
| **Air quality**                    | ✅          | ✅           | Norway      | ✅           | ❌              | ❌            | ❌             |
| **Pollen**                         | ✅²         | ✅           | ❌           | ❌           | ❌              | ❌            | ❌             |
| **Precipitation in the next hour** | ✅³         | ✅           | Nordic area | ✅           | ✅              | France       | ❌             |
| **Alerts**                         | ❌          | ✅           | ❌           | ✅           | ✅              | France       | *In progress* |
| **Normals**                        | ❌          | ✅           | ❌           | ❌           | ❌              | ✅⁴           | ❌             |

| Sources                            | China | NWS | Bright Sky | ECCC   | MET Éireann   | ATMO AURA     |
|------------------------------------|-------|-----|------------|--------|---------------|---------------|
| **Air quality**                    | China | ❌   | ❌          | ❌      | ❌             | France (AuRA) |
| **Pollen**                         | ❌     | ❌   | ❌          | ❌      | ❌             | ❌             |
| **Precipitation in the next hour** | China | ❌   | ❌          | ❌      | ❌             | ❌             |
| **Alerts**                         | China | ✅   | Germany    | Canada | *In progress* | ❌             |
| **Normals**                        | ❌     | ❌   | ❌          | Canada | ❌             | ❌             |

* ¹ Only supports NWS alerts, but has many duplicate issues, so not worth implementing
* ² Not restricted but currently only works in Europe
* ³ Works best in Europe at the moment
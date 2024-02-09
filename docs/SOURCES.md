# Weather sources

This is a user-end guide to weather sources available in Breezy Weather. If you are a developer looking to add a new source in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md).

**AccuWeather** is the most complete source, although you may not need so much completeness (not many people care about ceiling, for example).

**Open-Meteo** is the only free and open source weather source on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, however lacks major features (short-term forecast accuracy, alerts, realtime precipitations and reverse geocoding).


## Accuracy

When deciding about which source you want to use, accuracy of data is probably the most important criteria.

Here are some suggestions based on various criteria or testimonies (you can suggest others in GitHub discussions):

| Country/Continent | Main source                                      | Air quality | Pollen      | Minutely     | Alerts       | Normals      |
|-------------------|--------------------------------------------------|-------------|-------------|--------------|--------------|--------------|
| Europe            |                                                  |             | Open-Meteo  |              |              |              |
| United States     | [Forecast Advisor](https://forecastadvisor.com/) | AccuWeather | AccuWeather |              |              |              |
| Canada            | ECCC                                             | AccuWeather | AccuWeather |              | ECCC         | ECCC         |
| Germany           | Bright Sky (DWD)                                 | Open-Meteo  | Open-Meteo  |              | Bright Sky   |              |
| France            | Météo-France                                     |             | Open-Meteo  | Météo-France | Météo-France | Météo-France |
| Sweden            | SMHI or MET Norway                               | Open-Meteo  | Open-Meteo  | MET Norway   |              |              |
| DMI               | DMI or MET Norway                                | Open-Meteo  | Open-Meteo  | MET Norway   |              |              |
| Norway            | MET Norway                                       | MET Norway  | Open-Meteo  | MET Norway   |              |              |
| Ireland           | MET Éireann                                      | Open-Meteo  | Open-Meteo  |              |              |              |


## Status

| Sources       | Open-Meteo | AccuWeather | MET Norway | OpenWeather   | Pirate Weather | Météo-France | DMI       | China³ | Bright Sky | ECCC   | SMHI   | MET Éireann |
|---------------|------------|-------------|------------|---------------|----------------|--------------|-----------|--------|------------|--------|--------|-------------|
| **API key**   | None       | Optional    | None       | Rate-limited¹ | Rate-limited¹  | Optional     | None      | None   | None       | None   | None   | None        |
| **Countries** | Worldwide² | Worldwide²  | Worldwide² | Worldwide²    | Worldwide²     | Worldwide²   | Worldwide | China  | Germany    | Canada | Sweden | Ireland     |

Additional sources are available to configure but requires an API key. You can find them at the bottom of this page.

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features may not be available everywhere.
* ³ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC


## Main features

| Sources                       | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI | China | Bright Sky | ECCC | SMHI | MET Éireann |
|-------------------------------|------------|-------------|------------|-------------|--------------|-----|-------|------------|------|------|-------------|
| **Daily (days)**              | 15         | 15          | ~10        | 7 or 8      | 14           | 10  | 15    | 10         | 6    | 15   | 7           |
| **Hourly (days)**             | 16         | 10          | ~10        | 2           | 15           | 10  | 1     | 10         | 1    | 15   | 7           |
| **Weather**                   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅     | ✅          | ✅    | ✅    | ✅           |
| **Temperature**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅     | ✅          | ✅    | ✅    | ✅           |
| **Precipitation**             | ✅          | ✅ (RSI)     | ✅          | ✅ (RS)      | ✅ (RS)       | ✅   | ❌     | ✅          | ❌    | ✅    | ✅           |
| **Precipitation probability** | ✅          | ✅ (TRSI)    | ✅ (T)      | ✅           | ✅ (RSI)      | ❌   | Daily | ✅          | ✅    | T    | ❌           |
| **Wind**                      | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅     | ✅          | ✅    | ✅    | ✅           |
| **UV**                        | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   | ❌     | ❌          | ❌    | ❌    | ❌           |
| **Sun & Moon & Moon phase**   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅     | ✅          | ✅    | ✅    | ✅           |

Note that no forecast above 7 days is reliable, so you should not decide based on the highest number of days available.


## Features that can be added from other sources

The following features, if not available from your selected source, can be added from another source.

| Sources                            | Open-Meteo | AccuWeather   | MET Norway    | OpenWeather | Météo-France | DMI           | China    | Bright Sky | ECCC | SMHI | MET Éireann   |
|------------------------------------|------------|---------------|---------------|-------------|--------------|---------------|----------|------------|------|------|---------------|
| **Air quality**                    | ✅          | ✅             | Norway        | ✅           | ❌            | ❌             | Current  | ❌          | ❌    | ❌    | ❌             |
| **Pollen**                         | Europe     | North America | ❌             | ❌           | ❌            | ❌             | ❌        | ❌          | ❌    | ❌    | ❌             |
| **Precipitation in the next hour** | ✅²         | ✅             | Nordic area   | ✅           | France       | ❌             | ✅        | ❌          | ❌    | ❌    | ❌             |
| **Alerts**                         | ❌          | ✅             | *In progress* | ✅¹          | ✅            | *In progress* | ✅        | ✅          | ✅    | ❌    | *In progress* |
| **Normals**                        | Average¹   | ✅             | Average¹      | Average¹    | ✅            | ❌             | Average¹ | ❌          | ✅    | ❌    | ❌             |

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

| Sources                    | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI | China   | Bright Sky | ECCC    | SMHI | MET Éireann |
|----------------------------|------------|-------------|------------|-------------|--------------|-----|---------|------------|---------|------|-------------|
| **Humidity**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | Current | ✅          | Current | ✅    | ✅           |
| **Dew point**              | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | Current | ✅          | Current | ✅    | ✅           |
| **Pressure**               | ✅          | Current     | ✅          | ✅           | ✅            | ✅   | ❌       | ✅          | Current | ✅    | ✅           |
| **Cloud cover**            | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   | ❌       | ✅          | ❌       | ❌    | ❌           |
| **Visibility**             | ✅          | ✅           | ❌          | ✅           | ❌            | ✅   | Current | ✅          | Current | ✅    | ❌           |
| **Ceiling**                | ❌          | ✅           | ❌          | ❌           | ❌            | ❌   | ❌       | ❌          | ❌       | ❌    | ❌           |
| **Precipitation duration** | ❌          | ✅ (RSI)     | ❌          | ❌           | ❌            | ❌   | ❌       | ❌          | ❌       | ❌    | ❌           |
| **Hours of sun**           | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   | ✅       | ✅          | ✅       | ❌    | ❌           |

¹ Median from daily forecast


## Location

| Sources               | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI     | China | Bright Sky | ECCC    | SMHI    | MET Éireann |
|-----------------------|------------|-------------|------------|-------------|--------------|---------|-------|------------|---------|---------|-------------|
| **Search**            | ✅          | ✅           | Default    | Default     | Default      | Default | ✅³    | Default    | Default | Default | Default     |
| **Reverse geocoding** | ❌²         | ✅           | ❌²         | ❌²          | ✅²           | ✅       | ✅³    | ✅²         | ✅²      | ✅²      | ❌²          |

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
| **Alerts**                         | ✅              | US (NWS) |
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

| Sources                            | Open-Meteo | AccuWeather | MET Norway  | OpenWeather | Pirate Weather | Météo-France | Bright Sky | ECCC   | ATMO AURA     |
|------------------------------------|------------|-------------|-------------|-------------|----------------|--------------|------------|--------|---------------|
| **Air quality**                    | ✅          | ✅           | Norway      | ✅           | ❌              | ❌            | ❌          | ❌      | France (AuRA) |
| **Pollen**                         | ✅²         | ✅           | ❌           | ❌           | ❌              | ❌            | ❌          | ❌      | ❌             |
| **Precipitation in the next hour** | ✅³         | ✅           | Nordic area | ✅           | ✅              | France       | ❌          | ❌      | ❌             |
| **Alerts**                         | ❌          | ✅           | ❌           | ✅           | ✅              | France       | Germany    | Canada | ❌             |
| **Normals**                        | ❌          | ✅           | ❌           | ❌           | ❌              | ✅⁴           | ❌          | Canada | ❌             |

❌ means that it’s either not supported or doesn’t support longitude/latitude.

* ¹ Only supports NWS alerts, but has many duplicate issues, so not worth implementing
* ² Not restricted but currently only works in Europe
* ³ Works best in Europe at the moment
# Weather sources

This is a user-end guide to weather sources available in Breezy Weather. If you are a developer looking to add a new source in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md). Unless otherwise mentioned, **the information below is valid assuming you’re using version 5.2.8 or later of Breezy Weather**. Note: Current source and HKO source are only available starting from (unreleased) v5.3.0.

By default, when you add a location manually, Breezy Weather will auto-suggest your national weather source if we have support for it, and combine it with other secondary weather sources for missing features.

When we don’t have support for your national weather source, we suggest **Open-Meteo** which is the only free and open source weather source on this list, and probably also the most privacy-friendly. It is nearly as complete as **AccuWeather**, and usually more accurate for many countries, however still lacks a few features (station observations, alerts, reverse geocoding), which is why AccuWeather is also suggested for alerts and normals for some countries.

For United States of America, the [Forecast Advisor website](https://www.forecastadvisor.com/) has temperature and precipitation 1-3 days accuracy comparison by city of the following sources: AccuWeather, NWS, Open-Meteo and Pirate Weather.

Below, you can find details about the support and implementation status for features on each weather source.


## Status

| Worldwide sources² | Open-Meteo | AccuWeather | MET Norway | OpenWeather   | Pirate Weather | HERE     | Météo-France | DMI  | Meteo AM |
|--------------------|------------|-------------|------------|---------------|----------------|----------|--------------|------|----------|
| **API key**        | None       | Optional    | None       | Rate-limited¹ | Required       | Required | Optional     | None | None     |

| National sources | China³ | NWS  | GeoSphere Austria  | Bright Sky | ECCC   | CWA      | IMS                           | SMHI   | HKO       | MET Éireann |
|------------------|--------|------|--------------------|------------|--------|----------|-------------------------------|--------|-----------|-------------|
| **API key**      | None   | None | None               | None       | None   | Required | None                          | None   | None      | None        |
| **Countries**    | China  | USA  | Austria and nearby | Germany    | Canada | Taiwan   | Israel, West Bank, Gaza Strip | Sweden | Hong Kong | Ireland     |

* ¹ Bundled API key is often rate-limited. You can configure your own API key, however OpenWeather asks for credit card information even if you only want to use the free-tier.
* ² Some features may not be available everywhere.
* ³ Aggregated data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC


## Main features supported by each source

Sources with mandatory API key to use are at the bottom of this page.

| Worldwide sources             | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI | Meteo AM |
|-------------------------------|------------|-------------|------------|-------------|--------------|-----|----------|
| **Daily (days)**              | 15         | 15          | ~10        | 5           | 14           | 10  | 5        |
| **Hourly (days)**             | 16         | 10          | ~10        | 5           | 15           | 10  | 5        |
| **Weather**                   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅        |
| **Temperature**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅        |
| **Precipitation**             | ✅          | ✅ (RSI)     | ✅          | ✅ (RS)      | ✅ (RS)       | ✅   | ❌        |
| **Precipitation probability** | ✅          | ✅ (TRSI)    | ✅ (T)      | ✅           | ✅ (RSI)      | ❌   | ✅        |
| **Wind**                      | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅        |
| **UV**                        | ✅          | ✅           | ✅          | ❌           | ✅            | ❌   | ❌        |
| **Sun & Moon & Moon phase**   | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ❌        |

| National sources              | China | NWS    | GeoSphere Austria | Bright Sky | ECCC | IMS           | SMHI | HKO     | MET Éireann |
|-------------------------------|-------|--------|-------------------|------------|------|---------------|------|---------|-------------|
| **Daily (days)**              | 15    | 7      | 2.5               | 10         | 6    | 6             | 15   | 10      | 7           |
| **Hourly (days)**             | 1     | 7      | 2.5               | 10         | 1    | 6             | 15   | 10      | 7           |
| **Weather**                   | ✅     | ✅      | ✅                 | ✅          | ✅    | *In progress* | ✅    | ✅       | ✅           |
| **Temperature**               | ✅     | ✅      | ✅                 | ✅          | ✅    | ✅             | ✅    | ✅       | ✅           |
| **Precipitation**             | ❌     | ✅ (SI) | ✅                 | ✅          | ❌    | ❌             | ✅    | ❌       | ✅           |
| **Precipitation probability** | Daily | ✅ (T)  | ❌                 | ✅          | ✅    | ✅             | T    | ✅       | ❌           |
| **Wind**                      | ✅     | ✅      | ✅                 | ✅          | ✅    | ✅             | ✅    | ✅       | ✅           |
| **UV**                        | ❌     | ❌      | ❌                 | ❌          | ❌    | ✅             | ❌    | Current | ❌           |
| **Sun & Moon & Moon phase**   | ✅     | ✅      | ✅                 | ✅          | ✅    | ✅             | ✅    | ✅       | ✅           |

Note that no forecast above 7 days is reliable, so you should not decide based on the highest number of days available.


## Features that can be added from other sources

The following features, if not available from your selected source, can be added from another source.

| Worldwide sources            | Open-Meteo | AccuWeather   | MET Norway  | OpenWeather | Météo-France | DMI     | Meteo AM |
|------------------------------|------------|---------------|-------------|-------------|--------------|---------|----------|
| **Current**                  | ✅          | ✅             | Nordic Area | ✅           | ✅            | ❌       | ✅        |
| **Air quality**              | ✅          | ✅             | Norway      | ✅           | ❌            | ❌       | ❌        |
| **Pollen**                   | ✅          | North America | ❌           | ❌           | ❌            | ❌       | ❌        |
| **Precipitation nowcasting** | ✅          | ✅             | Nordic area | ❌           | France       | ❌       | ❌        |
| **Alerts**                   | ❌          | ✅             | ✅           | ❌           | France       | Denmark | ❌        |
| **Normals**                  | Average    | ✅             | Average     | Average     | ✅            | Average | ❌        |

| National sources             | China   | NWS           | GeoSphere Austria  | Bright Sky | ECCC   | IMS                           | SMHI    | HKO       | MET Éireann |
|------------------------------|---------|---------------|--------------------|------------|--------|-------------------------------|---------|-----------|-------------|
| **Current**                  | China   | ❌             | ❌                  | Germany    | Canada | Israel, West Bank, Gaza Strip | ❌       | Hong Kong | ❌           |
| **Air quality**              | Current | ❌             | Europe and nearby  | ❌          | ❌      | ❌                             | ❌       | ❌         | ❌           |
| **Pollen**                   | ❌       | ❌             | ❌                  | ❌          | ❌      | ❌                             | ❌       | ❌         | ❌           |
| **Precipitation nowcasting** | China   | ❌             | Austria and nearby | ❌          | ❌      | ❌                             | ❌       | ❌         | ❌           |
| **Alerts**                   | China   | United States | Austria            | Germany    | Canada | Israel, West Bank, Gaza Strip | ❌       | Hong Kong | Ireland     |
| **Normals**                  | Average | Average       | *In progress*      | Average    | Canada | Average                       | Average | Hong Kong | Average     |


Legend:

| Letter | Meaning      |
|--------|--------------|
| R      | Rain         |
| T      | Thunderstorm |
| S      | Snow         |
| I      | Ice          |


## Other weather data

| Worldwide sources          | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI | Meteo AM |
|----------------------------|------------|-------------|------------|-------------|--------------|-----|----------|
| **Humidity**               | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ✅        |
| **Dew point**              | ✅          | ✅           | ✅          | ✅           | ✅            | ✅   | ❌        |
| **Pressure**               | ✅          | Current     | ✅          | ✅           | ✅            | ✅   | ✅        |
| **Cloud cover**            | ✅          | ✅           | ✅          | ✅           | ✅            | ❌   | ❌        |
| **Visibility**             | ✅          | ✅           | ❌          | ✅           | ❌            | ✅   | ❌        |
| **Ceiling**                | ❌          | ✅           | ❌          | ❌           | ❌            | ❌   | ❌        |
| **Precipitation duration** | ❌          | ✅ (RSI)     | ❌          | ❌           | ❌            | ❌   | ❌        |
| **Sunshine duration**      | ✅          | ✅           | ❌          | ❌           | ❌            | ❌   | ❌        |

| National sources           | China   | NWS | GeoSphere Austria | Bright Sky | ECCC    | IMS | SMHI | HKO     | MET Éireann |
|----------------------------|---------|-----|-------------------|------------|---------|-----|------|---------|-------------|
| **Humidity**               | Current | ✅   | ✅                 | ✅          | Current | ✅   | ✅    | ✅       | ✅           |
| **Dew point**              | Current | ✅   | ✅                 | ✅          | Current | ✅   | ✅    | ✅       | ✅           |
| **Pressure**               | ❌       | ✅   | ✅                 | ✅          | Current | ❌   | ✅    | Current | ✅           |
| **Cloud cover**            | ❌       | ✅   | ✅                 | ✅          | ❌       | ❌   | ❌    | ❌       | ❌           |
| **Visibility**             | Current | ✅   | ❌                 | ✅          | Current | ❌   | ✅    | ❌       | ❌           |
| **Ceiling**                | ❌       | ❌   | ❌                 | ❌          | ❌       | ❌   | ❌    | ❌       | ❌           |
| **Precipitation duration** | ❌       | ❌   | ❌                 | ❌          | ❌       | ❌   | ❌    | ❌       | ❌           |
| **Sunshine duration**      | ❌       | ❌   | ❌                 | ✅          | ✅       | ❌   | ❌    | ❌       | ❌           |

¹ Median from daily forecast


## Location

| Worldwide sources     | Open-Meteo | AccuWeather | MET Norway | OpenWeather | Météo-France | DMI     | Meteo AM |
|-----------------------|------------|-------------|------------|-------------|--------------|---------|----------|
| **Search**            | ✅          | ✅           | Default    | Default     | Default      | Default | Default  |
| **Reverse geocoding** | ❌²         | ✅           | ❌²         | ❌²          | ✅²           | ✅       | ✅        |

| National sources      | China | NWS     | GeoSphere Austria | Bright Sky | ECCC    | IMS     | SMHI    | HKO     | MET Éireann |
|-----------------------|-------|---------|-------------------|------------|---------|---------|---------|---------|-------------|
| **Search**            | ✅³    | Default | Default           | Default    | Default | Default | Default | Default | Default     |
| **Reverse geocoding** | ✅³    | ✅       | ❌²                | ❌²         | ✅²      | ✅⁴      | ❌²      | ✅⁵      | ✅⁶          |

* ¹ Default means it will use the configured location search source in settings. By default, it is Open-Meteo.
* ² TimeZone is assumed to be the same as device
* ³ TimeZone is assumed to be Asia/Shanghai
* ⁴ TimeZone is assumed to be Asia/Jerusalem
* ⁵ TimeZone is assumed to be Asia/Hong_Kong
* ⁶ TimeZone is assumed to be Europe/Dublin


# Additional sources with mandatory API key

## Main features

| Worldwide sources             | Pirate Weather | HERE |
|-------------------------------|----------------|------|
| **Daily (days)**              | 8              | 6    |
| **Hourly (days)**             | 2              | 6    |
| **Weather**                   | ✅              | ✅    |
| **Temperature**               | ✅              | ✅    |
| **Precipitation**             | ✅ (RS)         | ✅    |
| **Precipitation probability** | ✅              | ✅    |
| **Wind**                      | ✅              | ✅    |
| **UV**                        | ✅              | ✅    |
| **Sun & Moon & Moon phase**   | ✅              | ✅    |

| National sources              | CWA |
|-------------------------------|-----|
| **Daily (days)**              | 7   |
| **Hourly (days)**             | 4   |
| **Weather**                   | ✅   |
| **Temperature**               | ✅   |
| **Precipitation**             | ❌   |
| **Precipitation probability** | ✅   |
| **Wind**                      | ✅   |
| **UV**                        | ✅¹  |
| **Sun & Moon & Moon phase**   | ✅²  |

* ¹ Forecast only
* ² No moon phase

## Features that can be added from other sources

| Worldwide sources            | Pirate Weather | HERE     |
|------------------------------|----------------|----------|
| **Current**                  | ✅              | ✅        |
| **Air quality**              | ❌              | ❌        |
| **Pollen**                   | ❌              | ❌        |
| **Precipitation nowcasting** | ✅              | ❌        |
| **Alerts**                   | ✅              | ❌        |
| **Normals**                  | Average¹       | Average¹ |

| National sources             | CWA     |
|------------------------------|---------|
| **Current**                  | Taiwan  |
| **Air quality**              | Current |
| **Pollen**                   | ❌       |
| **Precipitation nowcasting** | ❌       |
| **Alerts**                   | ✅       |
| **Normals**                  | ✅       |

* ¹ Median from daily forecast

## Other weather data

| Worldwide sources          | Pirate Weather | HERE |
|----------------------------|----------------|------|
| **Humidity**               | ✅              | ✅    |
| **Dew point**              | ✅              | ✅    |
| **Pressure**               | ✅              | ✅    |
| **Cloud cover**            | ✅              | ❌    |
| **Visibility**             | Current        | ✅    |
| **Ceiling**                | ❌              | ❌    |
| **Precipitation duration** | ❌              | ❌    |
| **Sunshine duration**      | ❌              | ❌    |

| National sources           | CWA     |
|----------------------------|---------|
| **Humidity**               | ✅       |
| **Dew point**              | ✅       |
| **Pressure**               | Current |
| **Cloud cover**            | ❌       |
| **Visibility**             | ❌       |
| **Ceiling**                | ❌       |
| **Precipitation duration** | ❌       |
| **Sunshine duration**      | ❌       |

## Location

| Worldwide sources          | Pirate Weather | HERE |
|----------------------------|----------------|------|
| **Search**                 | Default        | ✅    |
| **Reverse geocoding**      | ❌¹             | ✅    |

| National sources           | CWA |
|----------------------------|-----|
| **Search**                 | ❌   |
| **Reverse geocoding**      | ✅²  |

* ¹ TimeZone is assumed to be the same as device
* ² TimeZone is assumed to be Asia/Taipei

# Combinable sources

| Worldwide sources            | Open-Meteo | AccuWeather | MET Norway  | OpenWeather | Pirate Weather | Météo-France | DMI     | Meteo AM      | HERE          |
|------------------------------|------------|-------------|-------------|-------------|----------------|--------------|---------|---------------|---------------|
| **Current**                  | ✅          | ✅           | Nordic Area | ✅           | ✅              | France       | ❌       | *In progress* | *In progress* |
| **Air quality**              | ✅          | ✅           | Norway      | ✅           | ❌              | ❌            | ❌       | ❌             | ❌             |
| **Pollen**                   | ✅²         | ✅           | ❌           | ❌           | ❌              | ❌            | ❌       | ❌             | ❌             |
| **Precipitation nowcasting** | ✅³         | ✅           | Nordic area | ❌           | ✅              | France       | ❌       | ❌             | ❌             |
| **Alerts**                   | ❌          | ✅           | ✅           | ❌           | ✅              | France       | Denmark | ❌             | ❌             |
| **Normals**                  | ❌          | ✅           | ❌           | ❌           | ❌              | France       | ❌       | ❌             | ❌             |

| National sources             | China | NWS           | GeoSphere Austria  | WMO Severe Weather | Bright Sky | ECCC   | CWA    | IMS                           | HKO       | MET Éireann | ATMO AuRA     |
|------------------------------|-------|---------------|--------------------|--------------------|------------|--------|--------|-------------------------------|-----------|-------------|---------------|
| **Current**                  | China | ❌             | ❌                  | ❌                  | Germany    | Canada | Taiwan | Israel, West Bank, Gaza Strip | Hong Kong | ❌           | ❌             |
| **Air quality**              | China | ❌             | Europe and nearby  | ❌                  | ❌          | ❌      | Taiwan | ❌                             | ❌         | ❌           | France (AuRA) |
| **Pollen**                   | ❌     | ❌             | ❌                  | ❌                  | ❌          | ❌      | ❌      | ❌                             | ❌         | ❌           | ❌             |
| **Precipitation nowcasting** | China | ❌             | Austria and nearby | ❌                  | ❌          | ❌      | ❌      | ❌                             | ❌         | ❌           | ❌             |
| **Alerts**                   | China | United States | Austria            | ✅                  | Germany    | Canada | Taiwan | Israel, West Bank, Gaza Strip | Hong Kong | Ireland     | ❌             |
| **Normals**                  | ❌     | ❌             | *In progress*      | ❌                  | ❌          | Canada | Taiwan | ❌                             | Hong Kong | ❌           | ❌             |

* ¹ Only supports NWS alerts, but has many duplicate issues, so not worth implementing
* ² Not restricted but currently only works in Europe
* ³ Works best in Europe at the moment

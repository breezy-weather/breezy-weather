# Weather sources

This is a user-end guide to weather sources available in Breezy Weather. If you are a developer looking to add a new source in Breezy Weather, have a look at [contribute](../CONTRIBUTE.md). Unless otherwise mentioned, **the information below is valid assuming you’re using the latest version of Breezy Weather**.

By default, when you add a location manually, Breezy Weather will auto-suggest your national weather source if we have support for it, and combine it with other secondary weather sources for missing features. When we don’t have support for your national weather source, we suggest **Open-Meteo** which is the only free and open source weather source on this list, and probably also the most privacy-friendly.

Below, you can find details about the support and implementation status for features on each weather source. Note that no forecast above 7 days is reliable, so you should not decide based on the highest number of days available.

## Summary
| Country/Territory                  | Source                                                  | Supported features                                                                   |
|------------------------------------|---------------------------------------------------------|--------------------------------------------------------------------------------------|
| 🌐 Worldwide                       | [Open-Meteo](#open-meteo)                               | Forecast, Current, Air quality, Pollen, Search                                       |
| 🌐 Worldwide                       | [AccuWeather](#accuweather) 🔓                          | Forecast, Current, Air quality, Pollen, Nowcasting, Alerts, Normals, Search, Address |
| 🌐 Worldwide                       | [Android](#android)                                     | Address                                                                              |
| 🌐 Worldwide                       | [GeoNames](#geonames) 🔐                                | Search                                                                               |
| 🌐 Worldwide                       | [FOSS Public Alert Server](#foss-public-alert-server)   | Alerts                                                                               |
| 🌐 Worldwide                       | [NCEI](#national-centers-for-environmental-information) | Normals                                                                              |
| 🌐 Worldwide                       | [Nominatim](#nominatim)                                 | Address                                                                              |
| 🌐 Worldwide                       | [OpenWeather](#openweather) 🔓                          | Forecast, Current, Air quality                                                       |
| 🌐 Worldwide                       | [Pirate Weather](#pirate-weather) 🔐                    | Forecast, Current, Nowcasting, Alerts                                                |
| 🌐 Worldwide                       | [WMO Severe Weather](#wmo-severe-weather)               | Alerts                                                                               |
| 🇦🇽 Åland Islands                 | [FMI](#finnish-meteorological-institute)                | Forecast, Current, Air quality, Alerts, Normals                                      |
| 🇦🇩 Andorra                       | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇦🇹 Austria                       | [GeoSphere Austria](#geosphere-austria)                 | Forecast, Air quality, Nowcasting, Alerts                                            |
| 🇧🇩 Bangladesh                    | [BMD](#bangladesh-meteorological-department)            | Forecast, Address                                                                    |
| 🇧🇯 Benin                         | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇧🇫 Burkina Faso                  | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇧🇮 Burundi                       | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇨🇦 Canada                        | [ECCC](#environment-and-climate-change-canada)          | Forecast, Current, Alerts, Normals, Address                                          |
| 🇹🇩 Chad                          | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇨🇳 China                         | [China](#china)                                         | Forecast, Current, Air quality, Nowcasting, Alerts, Address                          |
| 🇨🇩 Democratic Republic of Congo  | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇩🇰 Denmark                       | [DMI](#danmarks-meteorologiske-institut)                | Forecast, Alerts, Address                                                            |
| 🇪🇪 Estonia                       | [Ilmateenistus](#ilmateenistus)                         | Forecast, Address                                                                    |
| 🇪🇪 Estonia                       | [EKUK](#ekuk)                                           | Air quality, Pollen (later this year)                                                |
| 🇪🇹 Ethiopia                      | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇫🇰 Falkland Is.                  | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇫🇴 Faroe Is.                     | [DMI](#danmarks-meteorologiske-institut)                | Forecast, Alerts, Address                                                            |
| 🇫🇮 Finland                       | [FMI](#finnish-meteorological-institute)                | Forecast, Current, Air quality, Alerts, Normals                                      |
| 🇫🇷 France                        | [Météo-France](#météo-france)                           | Forecast, Current, Nowcasting, Alerts, Normals, Address                              |
| 🇫🇷 France                        | [Atmo France](#atmo-france)                             | Pollen                                                                               |
| 🇫🇷 France                        | [Recosanté](#recosanté)                                 | Pollen                                                                               |
| 🇫🇷 France (Auvergne-Rhône-Alpes) | [Atmo Auvergne-Rhône-Alpes](#atmo)                      | Air Quality                                                                          |
| 🇫🇷 France (Grand Est)            | [ATMO GrandEst](#atmo)                                  | Air Quality                                                                          |
| 🇫🇷 France (Hauts-de-France)      | [Atmo Hauts-de-France](#atmo)                           | Air Quality                                                                          |
| 🇫🇷 France (PACA)                 | [AtmoSud](#atmo)                                        | Air Quality                                                                          |
| 🇬🇫 French Guiana                 | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇵🇫 French Polynesia              | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇬🇲 Gambia                        | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇩🇪 Germany                       | [Bright Sky](#bright-sky)                               | Forecast, Current, Alerts                                                            |
| 🇬🇭 Ghana                         | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇬🇮 Gibraltar                     | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇬🇱 Greenland                     | [DMI](#danmarks-meteorologiske-institut)                | Forecast, Alerts, Address                                                            |
| 🇬🇵 Guadeloupe                    | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇬🇺 Guam                          | [NWS](#national-weather-service)                        | Forecast, Current, Alerts, Address                                                   |
| 🇬🇬 Guernsey                      | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇬🇼 Guinea-Bissau                 | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇭🇰 Hong Kong                     | [HKO](#hong-kong-observatory)                           | Forecast, Current, Alerts, Normals, Address                                          |
| 🇭🇰 Hong Kong                     | [EPD](#environmental-protection-department)             | Air quality                                                                          |
| 🇮🇸 Iceland                       | [Veðurstofa Íslands](#veðurstofa-íslands)               | Forecast, Current, Alerts, Address                                                   |
| 🇮🇳 India                         | [IMD](#india-meteorological-department)                 | Forecast                                                                             |
| 🇮🇩 Indonesia                     | [BMKG](#bmkg)                                           | Forecast, Current, Air quality, Alerts, Address                                      |
| 🇮🇪 Ireland                       | [MET Éireann](#met-éireann)                             | Forecast, Alerts, Address                                                            |
| 🇮🇲 Isle of Man                   | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇮🇱 Israel                        | [IMS](#israel-meteorological-service)                   | Forecast, Current, Alerts, Address                                                   |
| 🇮🇹 Italy                         | [Meteo AM](#servizio-meteo-am)                          | Forecast, Current, Address                                                           |
| 🇯🇵 Japan                         | [JMA](#japan-meteorological-agency)                     | Forecast, Current, Alerts, Normals, Address                                          |
| 🇯🇪 Jersey                        | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇱🇻 Latvia                        | [LVĢMC](#lvģmc)                                         | Forecast, Current, Air quality, Address                                              |
| 🇱🇹 Lithuania                     | [LHMT](#lhmt)                                           | Forecast, Current, Alerts, Address                                                   |
| 🇱🇺 Luxembourg                    | [MeteoLux](#meteolux)                                   | Forecast, Current, Alerts, Address                                                   |
| 🇲🇴 Macao                         | [SMG](#serviços-meteorológicos-e-geofísicos)            | Forecast, Current, Air quality, Alerts, Normals                                      |
| 🇲🇼 Malawi                        | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇲🇱 Mali                          | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇲🇶 Martinique                    | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇾🇹 Mayotte                       | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇲🇨 Monaco                        | [Météo-France](#météo-france)                           | Forecast, Normals, Address                                                           |
| 🇲🇳 Mongolia                      | [NAMEM](#namem)                                         | Forecast, Current, Air quality, Normals, Address                                     |
| 🇳🇨 New Caledonia                 | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇳🇪 Niger                         | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇲🇵 Northern Mariana Is.          | [NWS](#national-weather-service)                        | Forecast, Current, Alerts, Address                                                   |
| 🇳🇴 Norway                        | [MET Norway](#met-norway)                               | Forecast, Nowcasting, Air quality, Alerts                                            |
| 🇵🇭 Philippines                   | [PAGASA](#pagasa)                                       | Forecast, Current                                                                    |
| 🇵🇹 Portugal                      | [IPMA](#instituto-português-do-mar-e-da-atmosfera)      | Forecast, Alerts, Address                                                            |
| 🇵🇷 Puerto Rico                   | [NWS](#national-weather-service)                        | Forecast, Current, Alerts, Address                                                   |
| 🇷🇪 Réunion                       | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇸🇲 San Marino                    | [Meteo AM](#servizio-meteo-am)                          | Forecast, Current, Address                                                           |
| 🇸🇨 Seychelles                    | [ClimWeb](#climweb)                                     | Alerts, Normals                                                                      |
| 🇸🇸 South Sudan                   | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇪🇸 Spain                         | [AEMET](#aemet) 🔐                                      | Forecast, Current, Normals                                                           |
| 🇧🇱 St. Barthélemy                | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇲🇫 St. Martin                    | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇵🇲 St. Pierre &amp; Miquelon     | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇸🇩 Sudan                         | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇸🇯 Svalbard &amp; Jan Mayen      | [MET Norway](#met-norway)                               | Forecast, Alerts                                                                     |
| 🇸🇪 Sweden                        | [SMHI](#smhi)                                           | Forecast                                                                             |
| 🇹🇼 Taiwan                        | [CWA](#central-weather-administration) 🔐               | Forecast, Current, Air quality, Alerts, Normals, Address                             |
| 🇹🇼 Taiwan                        | [NCDR](#national-center-for-disaster-reduction)         | Alerts                                                                               |
| 🇹🇼 Taiwan                        | [NLSC](#national-land-survey-and-mapping-center)        | Address                                                                              |
| 🇹🇬 Togo                          | [ClimWeb](#climweb)                                     | Alerts                                                                               |
| 🇹🇷 Türkiye                       | [MGM](#meteoroloji-genel-müdürlüğü)                     | Forecast, Current, Alerts, Normals, Address                                          |
| 🇬🇧 United Kingdom                | [Met Office](#met-office) 🔐                            | Forecast, Address                                                                    |
| 🇺🇸 United States                 | [NWS](#national-weather-service)                        | Forecast, Current, Alerts, Address                                                   |
| 🇻🇮 U.S. Virgin Is.               | [NWS](#national-weather-service)                        | Forecast, Current, Alerts, Address                                                   |
| 🇻🇦 Vatican City                  | [Meteo AM](#servizio-meteo-am)                          | Forecast, Current, Address                                                           |
| 🇼🇫 Wallis &amp; Futuna           | [Météo-France](#météo-france)                           | Forecast, Alert, Normals, Address                                                    |
| 🇿🇼 Zimbabwe                      | [ClimWeb](#climweb)                                     | Alerts                                                                               |

## Worldwide sources

### Open-Meteo
**[Open-Meteo](https://open-meteo.com/)** is a weather data provider based in Bürglen, Switzerland. It is the only free and open source weather source on this list, and probably also the most privacy-friendly. When we don’t support your national weather source, we suggest using **Open-Meteo** as your primary weather source.

| Feature                        | Detail                                                              |
|--------------------------------|---------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 15 days                                                       |
| ⏱️ **Hourly forecast**         | Up to 16 days                                                       |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**    |
| 😶‍🌫️ **Air quality**         | Available                                                           |
| 🤧 **Pollen**                  | Available in Europe, based on Copernicus data (details below)       |
| ☔ **Precipitation nowcasting** | Available (works best in Europe at the moment)                      |
| ⚠️ **Alerts**                  | Not available                                                       |
| 📊 **Normals**                 | Not available                                                       |
| 🧭 **Address lookup**          | Not available                                                       |

For the United States, [Forecast Advisor](https://www.forecastadvisor.com/) has temperature and precipitation 1-3 days accuracy comparison by city between the following sources: [AccuWeather](#accuweather), [NWS](#national-weather-service), **Open-Meteo** and [Pirate Weather](#pirate-weather).

<details><summary><h4>Details of available data from Open-Meteo</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ✅         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

<details><summary><h4>Details of available pollens from Open-Meteo</h4></summary>

| Pollen     |
|------------|
| Alder      |
| Birch      |
| Olive tree |
| Grass      |
| Mugwort    |
| Ragweed    |
</details>

### AccuWeather
> 🔐 **This source requires an API key.** Breezy Weather comes with a pre-bundled API key. However, you may also configure your own API key. [Register here](https://developer.accuweather.com/)

**[AccuWeather](https://www.accuweather.com/)** is a commercial weather data provider based in State College, Pennsylvania, United States.

| Feature                        | Detail                                                                                                |
|--------------------------------|-------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🌐 Worldwide (some features may not be available for all locations)                                   |
| 📆 **Daily forecast**          | Up to 15 days                                                                                         |
| ⏱️ **Hourly forecast**         | Up to 10 days                                                                                         |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                                      |
| 😶‍🌫️ **Air quality**         | Available                                                                                             |
| 🤧 **Pollen**                  | Available in U.S. (48 contiguous states only), Canada, and Europe for: Tree, Grass, Ragweed, and Mold |
| ☔ **Precipitation nowcasting** | Available                                                                                             |
| ⚠️ **Alerts**                  | Available                                                                                             |
| 📊 **Normals**                 | Available                                                                                             |
| 🧭 **Address lookup**          | Available                                                                                             |

For the United States, [Forecast Advisor](https://www.forecastadvisor.com/) has temperature and precipitation 1-3 days accuracy comparison by city between the following sources: **AccuWeather**, [NWS](#national-weather-service), [Open-Meteo](#open-meteo) and [Pirate Weather](#pirate-weather).

<details><summary><h4>Details of available data from AccuWeather</h4></summary>

| Data                      | Available   | Data              | Available |
|---------------------------|-------------|-------------------|-----------|
| Weather Condition         | ✅           | Humidity          | ✅         |
| Temperature               | ✅           | Dew Point         | ✅         |
| Precipitation             | ✅ (RSI)     | UV Index          | ✅         |
| Precipitation Probability | ✅ (TRSI)    | Sunshine Duration | ✅         |
| Precipitation Duration    | ✅ (RSI)     | Cloud Cover       | ✅         |
| Wind                      | ✅           | Visibility        | ✅         |
| Pressure                  | ✅ (Current) | Ceiling           | ✅         |
</details>

### OpenWeather
> 🔐 **This source requires an API key.** Breezy Weather comes with a pre-bundled API key. However, it is often rate-limited, so you may want to configure your own API key instead. [Register here](https://www.here.com/get-started/marketplace-listings/here-destination-weather)

**[OpenWeather](https://openweathermap.org/)** is a weather data provider based in London, United Kingdom.

| Feature                        | Detail                                                              |
|--------------------------------|---------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 5 days                                                        |
| ⏱️ **Hourly forecast**         | Up to 5 days                                                        |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**    |
| 😶‍🌫️ **Air quality**         | Available                                                           |
| 🤧 **Pollen**                  | Not available                                                       |
| ☔ **Precipitation nowcasting** | Not available                                                       |
| ⚠️ **Alerts**                  | Not available                                                       |
| 📊 **Normals**                 | Not available                                                       |
| 🧭 **Address lookup**          | Not available                                                       |

<details><summary><h4>Details of available data from OpenWeather</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅ (RS)    | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Pirate Weather
> 🔐 **This source requires an API key.** [Register here](https://pirate-weather.apiable.io/)

**[Pirate Weather](https://pirateweather.net/)** is a weather data provider based in Ontario, Canada. It serves as a drop-in replacement for Dark Sky API, which was shut down on March 31, 2023.

| Feature                        | Detail                                                              |
|--------------------------------|---------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 8 days                                                        |
| ⏱️ **Hourly forecast**         | Up to 2 days                                                        |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**    |
| 😶‍🌫️ **Air quality**         | Not available                                                       |
| 🤧 **Pollen**                  | Not available                                                       |
| ☔ **Precipitation nowcasting** | Available                                                           |
| ⚠️ **Alerts**                  | Available                                                           |
| 📊 **Normals**                 | Not available                                                       |
| 🧭 **Address lookup**          | Not available                                                       |

For the United States, [Forecast Advisor](https://www.forecastadvisor.com/) has temperature and precipitation 1-3 days accuracy comparison by city between the following sources: [AccuWeather](#accuweather), [NWS](#national-weather-service), [Open-Meteo](#open-meteo) and **Pirate Weather**.

<details><summary><h4>Details of available data from Pirate Weather</h4></summary>

| Data                      | Available | Data              | Available   |
|---------------------------|-----------|-------------------|-------------|
| Weather Condition         | ✅         | Humidity          | ✅           |
| Temperature               | ✅         | Dew Point         | ✅           |
| Precipitation             | ✅ (RS)    | UV Index          | ✅           |
| Precipitation Probability | ✅         | Sunshine Duration | ❌           |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅           |
| Wind                      | ✅         | Visibility        | ✅ (Current) |
| Pressure                  | ✅         | Ceiling           | ❌           |
</details>

## National sources
Unless otherwise specified, features in the following sources will only work for the intended countries and territories.

### AEMET

> 🔐 **This source requires an API key.** [Register here](https://opendata.aemet.es/centrodedescargas/inicio)

**[Agencia Estatal de Meteorología](https://www.aemet.es/)** (AEMET) is the official meteorological service of Spain.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇪🇸 Spain                                                       |
| 📆 **Daily forecast**          | Up to 7 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 2 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Not available                                                    |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Not available                                                    |

<details><summary><h4>Details of available data from AEMET</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Bangladesh Meteorological Department

**[Bangladesh Meteorological Department](https://live6.bmd.gov.bd/)** (BMD) is the official meteorological service of Bangladesh.

| Feature                        | Detail                                        |
|--------------------------------|-----------------------------------------------|
| 🗺️ **Coverage**               | 🇧🇩 Bangladesh                               |
| 📆 **Daily forecast**          | Up to 10 days                                 |
| ⏱️ **Hourly forecast**         | Up to 4 days                                  |
| ▶️ **Current observation**     | Not available: will show hourly forecast data |
| 😶‍🌫️ **Air quality**         | Not available                                 |
| 🤧 **Pollen**                  | Not available                                 |
| ☔ **Precipitation nowcasting** | Not available                                 |
| ⚠️ **Alerts**                  | Not available                                 |
| 📊 **Normals**                 | Not available                                 |
| 🧭 **Address lookup**          | Available                                     |

<details><summary><h4>Details of available data from BMD</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### BMKG

**[Badan Meteorologi, Klimatologi, dan Geofisika](https://www.bmkg.go.id/)** (BMKG) is the official meteorological service of Indonesia.

| Feature                        | Detail                                                                          |
|--------------------------------|---------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇩 Indonesia                                                                  |
| 📆 **Daily forecast**          | Up to 9 days                                                                    |
| ⏱️ **Hourly forecast**         | Up to 9 days                                                                    |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                |
| 😶‍🌫️ **Air quality**         | Available: current observation of PM2.5                                         |
| 🤧 **Pollen**                  | Not available                                                                   |
| ☔ **Precipitation nowcasting** | Not available                                                                   |
| ⚠️ **Alerts**                  | Available in Indonesian; Impact Based Forecast alerts also available in English |
| 📊 **Normals**                 | Not available                                                                   |
| 🧭 **Address lookup**          | Available                                                                       |

<details><summary><h4>Details of available data from BMKG</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### Bright Sky
**[Bright Sky](https://brightsky.dev/)** is a JSON API provider of open weather data from the [Deutsche Wetterdienst](https://www.dwd.de/) (DWD), the official meteorological service of Germany.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇩🇪 Germany                                                     |
| 📆 **Daily forecast**          | Up to 10 days                                                    |
| ⏱️ **Hourly forecast**         | Up to 10 days                                                    |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available in both English and German                             |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Not available                                                    |

<details><summary><h4>Details of available data from Bright Sky</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ✅         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Central Weather Administration
> 🔐 **This source requires an API key.** [Register here](https://opendata.cwa.gov.tw/)

**[Central Weather Administration](https://www.cwa.gov.tw/)** (CWA) is the official meteorological service of Taiwan.

| Feature                        | Detail                                                                                         |
|--------------------------------|------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇹🇼 Taiwan                                                                                    |
| 📆 **Daily forecast**          | Up to 7 days                                                                                   |
| ⏱️ **Hourly forecast**         | Up to 4 days                                                                                   |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                               |
| 😶‍🌫️ **Air quality**         | Available: current observation from the [Ministry of Environment](https://airtw.moenv.gov.tw/) |
| 🤧 **Pollen**                  | Not available                                                                                  |
| ☔ **Precipitation nowcasting** | Not available                                                                                  |
| ⚠️ **Alerts**                  | Partial coverage only: use [NCDR](#national-center-for-disaster-reduction) for full coverage   |
| 📊 **Normals**                 | Available                                                                                      |
| 🧭 **Address lookup**          | Available                                                                                      |

<details><summary><h4>Details of available data from CWA</h4></summary>

| Data                      | Available   | Data              | Available |
|---------------------------|-------------|-------------------|-----------|
| Weather Condition         | ✅           | Humidity          | ✅         |
| Temperature               | ✅           | Dew Point         | ✅         |
| Precipitation             | ❌           | UV Index          | ✅ (Daily) |
| Precipitation Probability | ✅ (4 days)  | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌           | Cloud Cover       | ❌         |
| Wind                      | ✅           | Visibility        | ❌         |
| Pressure                  | ✅ (Current) | Ceiling           | ❌         |
</details>

### China
This source aggregates data from Beijing Meteorological Service, ColorfulClouds (Caiyun) and CNEMC.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇨🇳 China                                                       |
| 📆 **Daily forecast**          | Up to 15 days                                                    |
| ⏱️ **Hourly forecast**         | Up to 1 day                                                      |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Available: current observation                                   |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Available                                                        |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from China source</h4></summary>

| Data                      | Available | Data              | Available   |
|---------------------------|-----------|-------------------|-------------|
| Weather Condition         | ✅         | Humidity          | ✅ (Current) |
| Temperature               | ✅         | Dew Point         | ✅ (Current) |
| Precipitation             | ❌         | UV Index          | ❌           |
| Precipitation Probability | ✅ (Daily) | Sunshine Duration | ❌           |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌           |
| Wind                      | ✅         | Visibility        | ✅ (Current) |
| Pressure                  | ❌         | Ceiling           | ❌           |
</details>

### Danmarks Meteorologiske Institut
**[Danmarks Meteorologiske Institut](https://www.dmi.dk/)** (DMI) is the official meteorological service of Denmark, the Faroe Islands, and Greenland.

| Feature                        | Detail                                                                                                                    |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇩🇰 Denmark, 🇫🇴 Faroe Islands, 🇬🇱 Greenland, and 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 10 days                                                                                                             |
| ⏱️ **Hourly forecast**         | Up to 10 days                                                                                                             |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                                                          |
| 😶‍🌫️ **Air quality**         | Not available                                                                                                             |
| 🤧 **Pollen**                  | Not available                                                                                                             |
| ☔ **Precipitation nowcasting** | Not available                                                                                                             |
| ⚠️ **Alerts**                  | Available for Denmark                                                                                                     |
| 📊 **Normals**                 | Not available                                                                                                             |
| 🧭 **Address lookup**          | Available                                                                                                                 |

<details><summary><h4>Details of available data from DMI</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Environment and Climate Change Canada
**[Environment and Climate Change Canada](https://www.canada.ca/en/environment-climate-change.html)** is the Canadian governmental department responsible for providing meteorological information, including [daily weather forecast and warnings](https://weather.gc.ca/), to all of Canada.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇨🇦 Canada                                                      |
| 📆 **Daily forecast**          | Up to 6 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 1 day                                                      |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from ECCC</h4></summary>

| Data                      | Available   | Data              | Available   |
|---------------------------|-------------|-------------------|-------------|
| Weather Condition         | ✅           | Humidity          | ✅ (Current) |
| Temperature               | ✅           | Dew Point         | ✅ (Current) |
| Precipitation             | ❌           | UV Index          | ✅           |
| Precipitation Probability | ✅           | Sunshine Duration | ✅           |
| Precipitation Duration    | ❌           | Cloud Cover       | ❌           |
| Wind                      | ✅           | Visibility        | ✅ (Current) |
| Pressure                  | ✅ (Current) | Ceiling           | ❌           |
</details>

### Finnish Meteorological Institute
> This source will be available in an upcoming release.

**[Finnish Meteorological Institute](https://www.ilmatieteenlaitos.fi/)** is the official meteorological service of Finland, and the Åland Islands.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇫🇮 Finland, 🇦🇽 Åland Islands ; air quality for 🌍 Europe     |
| 📆 **Daily forecast**          | Up to 9 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 9 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Up to 4 days                                                     |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Not available                                                    |

<details><summary><h4>Details of available data from Finnish Meteorological Institute</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | Current   |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### GeoSphere Austria
**[GeoSphere Austria](https://www.geosphere.at/de)** is the official meteorological service of Austria. It is formed out of the combination of *Zentralanstalt für Meteorologie und Geodynamik* (ZAMG) and *Geologische Bundesanstalt* (GBA) in 2023.

| Feature                        | Detail                                                                                |
|--------------------------------|---------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇦🇹 Austria; air quality and precipitation nowcast for nearby locations in 🌍 Europe |
| 📆 **Daily forecast**          | Up to 2.5 days                                                                        |
| ⏱️ **Hourly forecast**         | Up to 2.5 days                                                                        |
| ▶️ **Current observation**     | Not available: will show hourly forecast data                                         |
| 😶‍🌫️ **Air quality**         | Available for Europe and nearby                                                       |
| 🤧 **Pollen**                  | Not available                                                                         |
| ☔ **Precipitation nowcasting** | Available for Austria and nearby                                                      |
| ⚠️ **Alerts**                  | Available                                                                             |
| 📊 **Normals**                 | 🚧 *(in progress)* 🚧                                                                 |
| 🧭 **Address lookup**          | Not available                                                                         |

<details><summary><h4>Details of available data from Geosphere Austria</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Hong Kong Observatory

**[Hong Kong Observatory](https://www.hko.gov.hk/)** (HKO) is the official meteorological service of Hong Kong.

| Feature                        | Detail                                                                                                                                             |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇭🇰 Hong Kong                                                                                                                                     |
| 📆 **Daily forecast**          | Up to 9 days                                                                                                                                       |
| ⏱️ **Hourly forecast**         | Up to 9 days                                                                                                                                       |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                                                                                   |
| 😶‍🌫️ **Air quality**         | Not available: Users can add [EPD](#environmental-protection-department) instead                                                                   |
| 🤧 **Pollen**                  | Not available                                                                                                                                      |
| ☔ **Precipitation nowcasting** | Not available                                                                                                                                      |
| ⚠️ **Alerts**                  | Available in English, Traditional Chinese, and Simplified Chinese. Alert headlines are additionally available in Hindi, Indonesian, and Vietnamese |
| 📊 **Normals**                 | Available                                                                                                                                          |
| 🧭 **Address lookup**          | Available                                                                                                                                          |

<details><summary><h4>Details of available data from HKO</h4></summary>

| Data                      | Available   | Data              | Available   |
|---------------------------|-------------|-------------------|-------------|
| Weather Condition         | ✅           | Humidity          | ✅           |
| Temperature               | ✅           | Dew Point         | ✅           |
| Precipitation             | ❌           | UV Index          | ✅ (Current) |
| Precipitation Probability | ✅ (Daily)   | Sunshine Duration | ❌           |
| Precipitation Duration    | ❌           | Cloud Cover       | ❌           |
| Wind                      | ✅           | Visibility        | ❌           |
| Pressure                  | ✅ (Current) | Ceiling           | ❌           |
</details>

### Ilmateenistus

**[Ilmateeniustus](https://www.ilmateenistus.ee/)** is the official meteorological service of Estonia.

| Feature                        | Detail                                                                         |
|--------------------------------|--------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇪🇪 Estonia                                                                   |
| 📆 **Daily forecast**          | Up to 4 days                                                                   |
| ⏱️ **Hourly forecast**         | Up to 4 days                                                                   |
| ▶️ **Current observation**     | Not available: will show hourly forecast data                                  |
| 😶‍🌫️ **Air quality**         | Not available: Users can add [EKUK](#ekuk) instead                             |
| 🤧 **Pollen**                  | Not available: Users will be able to add [EKUK](#ekuk) instead later this year |
| ☔ **Precipitation nowcasting** | Not available                                                                  |
| ⚠️ **Alerts**                  | Not available                                                                  |
| 📊 **Normals**                 | Not available                                                                  |
| 🧭 **Address lookup**          | Available                                                                      |

<details><summary><h4>Details of available data from Ilmateenistus</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ❌         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### India Meteorological Department

**[India Meteorological Department](https://mausam.imd.gov.in/)** (IMD) is the official meteorological service of India.

| Feature                        | Detail                                                   |
|--------------------------------|----------------------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇳 India                                               |
| 📆 **Daily forecast**          | Up to 10 days                                            |
| ⏱️ **Hourly forecast**         | Up to 10 days                                            |
| ▶️ **Current observation**     | Not available: will show hourly forecast data            |
| 😶‍🌫️ **Air quality**         | Not available                                            |
| 🤧 **Pollen**                  | Not available                                            |
| ☔ **Precipitation nowcasting** | Not available                                            |
| ⚠️ **Alerts**                  | Not available                                            |
| 📊 **Normals**                 | Not available                                            |
| 🧭 **Address lookup**          | Not available                                            |

<details><summary><h4>Details of available data from IMD</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### Instituto Português do Mar e da Atmosfera

**[Instituto Português do Mar e da Atmosfera](https://www.ipma.pt/)** (IPMA) is the official meteorological service of Portugal.

| Feature                        | Detail                                        |
|--------------------------------|-----------------------------------------------|
| 🗺️ **Coverage**               | 🇵🇹 Portugal                                 |
| 📆 **Daily forecast**          | Up to 10 days                                 |
| ⏱️ **Hourly forecast**         | Up to 5 days                                  |
| ▶️ **Current observation**     | Not available: will show hourly forecast data |
| 😶‍🌫️ **Air quality**         | Not available                                 |
| 🤧 **Pollen**                  | Not available                                 |
| ☔ **Precipitation nowcasting** | Not available                                 |
| ⚠️ **Alerts**                  | Available                                     |
| 📊 **Normals**                 | Not available                                 |
| 🧭 **Address lookup**          | Available                                     |

<details><summary><h4>Details of available data from IPMA</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ❌         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### Israel Meteorological Service
**[Israel Meteorological Service](https://ims.gov.il/)** (IMS) is the official meteorological service of Israel.

| Feature                        | Detail                                                                                 |
|--------------------------------|----------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇱 Israel, the West Bank, Gaza Strip                                                 |
| 📆 **Daily forecast**          | Up to 6 days                                                                           |
| ⏱️ **Hourly forecast**         | Up to 6 days                                                                           |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                       |
| 😶‍🌫️ **Air quality**         | Not available                                                                          |
| 🤧 **Pollen**                  | Not available                                                                          |
| ☔ **Precipitation nowcasting** | Not available                                                                          |
| ⚠️ **Alerts**                  | Available in English and Hebrew. Alert headlines are additionally available in Arabic. |
| 📊 **Normals**                 | Not available                                                                          |
| 🧭 **Address lookup**          | Available                                                                              |

<details><summary><h4>Details of available data from IMS</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ❌         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### Japan Meteorological Agency

**[Japan Meteorological Agency](https://www.jma.go.jp/)** (JMA) is the official meteorological service of Japan.

| Feature                        | Detail                                                                                  |
|--------------------------------|-----------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇯🇵 Japan                                                                              |
| 📆 **Daily forecast**          | Up to 7 days                                                                            |
| ⏱️ **Hourly forecast**         | Up to 2 days                                                                            |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                        |
| 😶‍🌫️ **Air quality**         | Not available                                                                           |
| 🤧 **Pollen**                  | Not available                                                                           |
| ☔ **Precipitation nowcasting** | Not available                                                                           |
| ⚠️ **Alerts**                  | Available in Japanese. Alert headlines are additionally available in multiple languages |
| 📊 **Normals**                 | Available                                                                               |
| 🧭 **Address lookup**          | Available                                                                               |

<details><summary><h4>Details of available data from IPMA</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ❌         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### LHMT

**[Lietuvos hidrometeorologijos tarnyba](https://www.meteo.lt/)** (LHMT) is the official meteorological service of Lithuania.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇱🇹 Lithuania                                                   |
| 📆 **Daily forecast**          | Up to 7 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 7 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from LHMT</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### LVĢMC

**[Latvijas Vides, ģeoloģijas un meteoroloģijas centrs](https://videscentrs.lvgmc.lv/)** (LVĢMC) is the official meteorological service of Latvia.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇱🇻 Latvia                                                      |
| 📆 **Daily forecast**          | Up to 10 days                                                    |
| ⏱️ **Hourly forecast**         | Up to 10 days                                                    |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Available                                                        |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | 🚧 *in progress*                                                 |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from LVĢMC</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Met Éireann
**[Met Éireann](https://www.met.ie/)** is the official meteorological service of Ireland.

| Feature                        | Detail                                        |
|--------------------------------|-----------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇪 Ireland                                  |
| 📆 **Daily forecast**          | Up to 7 days                                  |
| ⏱️ **Hourly forecast**         | Up to 7 days                                  |
| ▶️ **Current observation**     | Not available: will show hourly forecast data |
| 😶‍🌫️ **Air quality**         | Not available                                 |
| 🤧 **Pollen**                  | Not available                                 |
| ☔ **Precipitation nowcasting** | Not available                                 |
| ⚠️ **Alerts**                  | Available                                     |
| 📊 **Normals**                 | Not available                                 |
| 🧭 **Address lookup**          | Available                                     |

<details><summary><h4>Details of available data from MET Éireann</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Météo-France
**[Météo-France](https://meteofrance.com/)** is the official meteorological service of France and its overseas territories.

| Feature                        | Detail                                                                                                                                                               |
|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇫🇷 France, 🇦🇩 Andorra, 🇲🇨 Monaco and 🌐 Worldwide (some features may not be available for all locations)                                                       |
|                                | _Overseas departments:_ 🇬🇫 French Guiana, 🇬🇵 Guadeloupe, 🇲🇶 Martinique, 🇾🇹 Mayotte, 🇷🇪 Réunion                                                             |
|                                | _Overseas collectivities:_ 🇵🇫 French Polynesia, 🇳🇨 New Caledonia, 🇧🇱 St. Barthélemy, 🇲🇫 St. Martin, 🇵🇲 St. Pierre &amp; Miquelon, 🇼🇫 Wallis &amp; Futuna |
| ▶️ **Current observation**     | Available for Metropolitan France: can complement another source as a **Current Source**                                                                             |
| 📆 **Daily forecast**          | Up to 14 days                                                                                                                                                        |
| ⏱️ **Hourly forecast**         | Up to 15 days                                                                                                                                                        |
| 😶‍🌫️ **Air quality**         | Not available                                                                                                                                                        |
|                                | Users in some regions can add [Atmo sources](#atmo) as a secondary source                                                                                            |
| 🤧 **Pollen**                  | Not available                                                                                                                                                        |
| ☔ **Precipitation nowcasting** | Available for Metropolitan France                                                                                                                                    |
| ⚠️ **Alerts**                  | Available for France and its overseas territories, as well as Andorra                                                                                                |
| 📊 **Normals**                 | Available for France and its overseas territories, as well as Andorra and Monaco                                                                                     |
| 🧭 **Address lookup**          | Available                                                                                                                                                            |

<details><summary><h4>Details of available data from Météo-France</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅ (RS)    | UV Index          | ✅         |
| Precipitation Probability | ✅ (RSI)   | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### MeteoLux

**[MeteoLux](https://www.meteolux.lu/)** is the official meteorological service of Luxembourg. It provides weather alerts in English, French, and German.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇱🇺 Luxembourg                                                  |
| 📆 **Daily forecast**          | Up to 5 days                                                     |
| ⏱️ **Hourly forecast**         | 6-hourly, up to 5 days                                           |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available in English, French, and German                         |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from MeteoLux</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ❌         | Sunshine Duration | ✅         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ❌         | Ceiling           | ❌         |
</details>

### Meteoroloji Genel Müdürlüğü

**[Meteoroloji Genel Müdürlüğü](https://www.mgm.gov.tr/)** (MGM) is the official meteorological service of Türkiye.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇹🇷 Türkiye                                                     |
| 📆 **Daily forecast**          | Up to 5 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 1.5 days                                                   |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from MGM</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ❌         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### MET Norway
**[Meteorologisk institutt](https://www.met.no/)** (MET Norway) is the official meteorological service of Norway.

| Feature                        | Detail                                                                              |
|--------------------------------|-------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇳🇴 Norway and 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 10 days                                                                       |
| ⏱️ **Hourly forecast**         | Up to 10 days                                                                       |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                    |
| 😶‍🌫️ **Air quality**         | Available for Norway                                                                |
| 🤧 **Pollen**                  | Not available                                                                       |
| ☔ **Precipitation nowcasting** | Available for the Nordic region                                                     |
| ⚠️ **Alerts**                  | Available for Norway                                                                |
| 📊 **Normals**                 | Not available                                                                       |
| 🧭 **Address lookup**          | Not available                                                                       |

<details><summary><h4>Details of available data from MET Norway</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Met Office

> 🔐 **This source requires an API key.** [Register here](https://datahub.metoffice.gov.uk/)

**[Met Office](https://www.metoffice.gov.uk/)** is the official meteorological service of the United Kingdom.

| Feature                        | Detail                                                                                                                 |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇬🇧 United Kingdom, 🇬🇬 Guernsey, 🇯🇪 Jersey, 🇮🇲 Isle of Man, 🇬🇮 Gibraltar, 🇫🇰 Falkland Is., and 🌐 Worldwide |
| 📆 **Daily forecast**          | Up to 7 days                                                                                                           |
| ⏱️ **Hourly forecast**         | Up to 2 days                                                                                                           |
| ▶️ **Current observation**     | Not available: will show hourly forecast data                                                                          |
| 😶‍🌫️ **Air quality**         | Not available                                                                                                          |
| 🤧 **Pollen**                  | Not available                                                                                                          |
| ☔ **Precipitation nowcasting** | Not available                                                                                                          |
| ⚠️ **Alerts**                  | Not available                                                                                                          |
| 📊 **Normals**                 | Not available                                                                                                          |
| 🧭 **Address lookup**          | Available                                                                                                              |

<details><summary><h4>Details of available data from Met Office</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ✅         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### NAMEM

**[National Agency for Meteorology and Environmental Monitoring](https://www.weather.gov.mn/)** (NAMEM) is the official meteorological service of Mongolia.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇲🇳 Mongolia                                                    |
| 📆 **Daily forecast**          | Up to 5 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 5 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Available for Ulaanbaatar and Erdenet                            |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Not available                                                    |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from NAMEM</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### National Weather Service
**[National Weather Service](https://www.weather.gov/)** (NWS) is the official meteorological service of the United States and its territories.

| Feature                        | Detail                                                                                                   |
|--------------------------------|----------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇺🇸 United States, 🇵🇷 Puerto Rico, 🇻🇮 U.S. Virgin Islands, 🇬🇺 Guam, 🇲🇵 Northern Mariana Islands |
| 📆 **Daily forecast**          | Up to 7 days                                                                                             |
| ⏱️ **Hourly forecast**         | Up to 7 days                                                                                             |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                                         |
| 😶‍🌫️ **Air quality**         | Not available                                                                                            |
| 🤧 **Pollen**                  | Not available                                                                                            |
| ☔ **Precipitation nowcasting** | Not available                                                                                            |
| ⚠️ **Alerts**                  | Available                                                                                                |
| 📊 **Normals**                 | Not available: Users can add [NCEI](#national-centers-for-environmental-information) instead             |
| 🧭 **Address lookup**          | Available                                                                                                |

For the United States, [Forecast Advisor](https://www.forecastadvisor.com/) has temperature and precipitation 1-3 days accuracy comparison by city between the following sources: [AccuWeather](#accuweather), **NWS**, [Open-Meteo](#open-meteo) and [Pirate Weather](#pirate-weather).

<details><summary><h4>Details of available data from NWS</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅ (SI)    | UV Index          | ❌         |
| Precipitation Probability | ✅ (T)     | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### PAGASA

**[Philippine Atmospheric, Geophysical and Astronomical Services Administration](https://www.pagasa.dost.gov.ph/)** (PAGASA) is the official meteorological service of the Philippines.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇵🇭 Philippines                                                 |
| 📆 **Daily forecast**          | Up to 5 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 5 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Not available                                                    |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Not available                                                    |

<details><summary><h4>Details of available data from PAGASA</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Serviços Meteorológicos e Geofísicos

**[Direcção dos Serviços Meteorológicos e Geofísicos](https://www.smg.gov.mo/)** (SMG) is the official meteorological service of Macao.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇲🇴 Macao                                                       |
| 📆 **Daily forecast**          | Up to 7 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 2 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Available: current observation                                   |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available in English, Traditional Chinese, and Portuguese        |
| 📊 **Normals**                 | Available                                                        |
| 🧭 **Address lookup**          | Not available                                                    |

<details><summary><h4>Details of available data from SMG</h4></summary>

| Data                      | Available   | Data              | Available   |
|---------------------------|-------------|-------------------|-------------|
| Weather Condition         | ✅           | Humidity          | ✅           |
| Temperature               | ✅           | Dew Point         | ✅           |
| Precipitation             | ❌           | UV Index          | ✅ (Current) |
| Precipitation Probability | ❌           | Sunshine Duration | ❌           |
| Precipitation Duration    | ❌           | Cloud Cover       | ❌           |
| Wind                      | ✅           | Visibility        | ❌           |
| Pressure                  | ✅ (Current) | Ceiling           | ❌           |
</details>

### Servizio Meteo AM
**[Servizio Meteo dell’Aeronautica Militare](https://www.meteoam.it/)** (Meteo AM) is the official meteorological service of Italy, San Marino, and Vatican City.

| Feature                        | Detail                                                                                                                  |
|--------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇹 Italy, 🇸🇲 San Marino, 🇻🇦 Vatican City, and 🌐 Worldwide (some features may not be available for all locations) |
| 📆 **Daily forecast**          | Up to 5 days                                                                                                            |
| ⏱️ **Hourly forecast**         | Up to 5 days                                                                                                            |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source**                                                        |
| 😶‍🌫️ **Air quality**         | Not available                                                                                                           |
| 🤧 **Pollen**                  | Not available                                                                                                           |
| ☔ **Precipitation nowcasting** | Not available                                                                                                           |
| ⚠️ **Alerts**                  | Not available                                                                                                           |
| 📊 **Normals**                 | Not available                                                                                                           |
| 🧭 **Address lookup**          | Available                                                                                                               |

<details><summary><h4>Details of available data from Meteo AM</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ❌         |
| Precipitation             | ❌         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ❌         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### SMHI
**[Sveriges meteorologiska och hydrologiska institut](https://www.smhi.se/)** (SMHI) is the official meteorological service of Sweden.

| Feature                        | Detail                                                   |
|--------------------------------|----------------------------------------------------------|
| 🗺️ **Coverage**               | 🇸🇪 Sweden                                              |
| 📆 **Daily forecast**          | Up to 15 days                                            |
| ⏱️ **Hourly forecast**         | Up to 15 days                                            |
| ▶️ **Current observation**     | Not available: will show hourly forecast data            |
| 😶‍🌫️ **Air quality**         | Not available                                            |
| 🤧 **Pollen**                  | Not available                                            |
| ☔ **Precipitation nowcasting** | Not available                                            |
| ⚠️ **Alerts**                  | Not available                                            |
| 📊 **Normals**                 | Not available                                            |
| 🧭 **Address lookup**          | Not available                                            |

<details><summary><h4>Details of available data from SMHI</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ✅         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ❌         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

### Veðurstofa Íslands
**[Veðurstofa Íslands](https://gottvedur.is/)** is the official meteorological service of Iceland.

| Feature                        | Detail                                                           |
|--------------------------------|------------------------------------------------------------------|
| 🗺️ **Coverage**               | 🇮🇸 Iceland                                                     |
| 📆 **Daily forecast**          | Up to 7 days                                                     |
| ⏱️ **Hourly forecast**         | Up to 7 days                                                     |
| ▶️ **Current observation**     | Available: can complement another source as a **Current Source** |
| 😶‍🌫️ **Air quality**         | Not available                                                    |
| 🤧 **Pollen**                  | Not available                                                    |
| ☔ **Precipitation nowcasting** | Not available                                                    |
| ⚠️ **Alerts**                  | Available                                                        |
| 📊 **Normals**                 | Not available                                                    |
| 🧭 **Address lookup**          | Available                                                        |

<details><summary><h4>Details of available data from Veðurstofa Íslands</h4></summary>

| Data                      | Available | Data              | Available |
|---------------------------|-----------|-------------------|-----------|
| Weather Condition         | ✅         | Humidity          | ✅         |
| Temperature               | ✅         | Dew Point         | ✅         |
| Precipitation             | ✅         | UV Index          | ❌         |
| Precipitation Probability | ❌         | Sunshine Duration | ❌         |
| Precipitation Duration    | ❌         | Cloud Cover       | ✅         |
| Wind                      | ✅         | Visibility        | ✅         |
| Pressure                  | ✅         | Ceiling           | ❌         |
</details>

## Secondary sources

### Android
**Android** can provide on some devices the address lookup feature. It uses the native Geocoder APIs. However, a backend for this geocoder is necessary for this to work.
Google Play Services usually provide this backend. Some OS with only a partial subset of Google Play Services features, such as GrapheneOS, don’t have a backend implementation.

This source doesn’t provide timezone support, so Breezy Weather will fallback to device’s timezone.

### ATMO
ATMO sources can be added as an **Air Quality** source for some regions of France.

- **[Atmo Auvergne-Rhône-Alpes](https://www.atmo-auvergnerhonealpes.fr/)** provides air quality information for the French region of Auvergne-Rhône-Alpes.
- **[ATMO GrandEst](https://www.atmo-grandest.eu/)** provides air quality information for the French region of Grand Est.
- **[Atmo Hauts-de-France](https://www.atmo-hdf.fr/)** provides air quality information for the French region of Hauts-de-France.
- **[AtmoSud](https://www.atmosud.org/)** provides air quality information for the French region of Provence-Alpes-Côte d’Azur.

### Atmo France
**[Atmo France](https://www.atmo-france.org/)** can be added as a **Pollen** source for France. Pollen concentration is calculated from Copernicus data.

<details><summary><h4>Details of available pollens from Atmo France</h4></summary>

| Pollen               | Availability                        |
|----------------------|-------------------------------------|
| Alder (Aulne)        | January to June                     |
| Birch (Bouleau)      | March to June                       |
| Olive tree (Olivier) | April to June                       |
| Grass (Graminées)    | March to August (peak in June-July) |
| Mugwort (Armoise)    | June to October                     |
| Ragweed (Ambroisie)  | June to October (as early as April) |
</details>

### ClimWeb
**[ClimWeb](https://github.com/wmo-raf/climweb)** is an open source content management system developed by WMO Africa for 17 of its member states:

| Country/Territory                 | Agency                                     |
|-----------------------------------|--------------------------------------------|
| 🇧🇯 Benin                        | [Météo Benin](http://www.meteobenin.bj/)   |
| 🇧🇫 Burkina Faso                 | [ANAM-BF](https://www.meteoburkina.bf/)    |
| 🇧🇮 Burundi                      | [IGEBU](https://www.igebu.bi/)             |
| 🇹🇩 Chad                         | [Météo Tchad](https://www.meteotchad.org/) |
| 🇨🇩 Democratic Republic of Congo | [Mettelsat](https://www.meteordcongo.cd/)  |
| 🇪🇹 Ethiopia                     | [EMI](https://www.ethiomet.gov.et/)        |
| 🇬🇲 Gambia                       | [DWR](https://meteogambia.org/)            |
| 🇬🇭 Ghana                        | [GMet](https://www.meteo.gov.gh/)          |
| 🇬🇼 Guinea-Bissau                | [INM](https://www.meteoguinebissau.org/)   |
| 🇲🇼 Malawi                       | [DCCMS](https://www.metmalawi.gov.mw/)     |
| 🇲🇱 Mali                         | [Mali-Météo](https://malimeteo.ml/)        |
| 🇳🇪 Niger                        | [DMN](https://www.niger-meteo.ne/)         |
| 🇸🇨 Seychelles                   | [SMA](https://www.meteo.gov.sc/)           |
| 🇸🇸 South Sudan                  | [SSMS](https://meteosouthsudan.com.ss/)    |
| 🇸🇩 Sudan                        | [SMA](https://meteosudan.sd/)              |
| 🇹🇬 Togo                         | [Météo Togo](https://www.anamet-togo.com/) |
| 🇿🇼 Zimbabwe                     | [MSD](https://www.weatherzw.org.zw/)       |

These sources can be added as an **Alert** and **Temperature normals** source for their respective countries.

### EKUK
**[Eesti Keskkonnauuringute Keskus](https://www.ohuseire.ee/)** (EKUK) can be added as an **Air quality** and **Pollen** (later this year) source for Estonia.

### Environmental Protection Department
**[Environmental Protection Department](https://www.aqhi.gov.hk/)** can be added as an **Air quality** source for Hong Kong.

### FOSS Public Alert Server
> The FOSS Public Alert Server project is currently in early testing.

**[FOSS Public Alert Server](https://invent.kde.org/webapps/foss-public-alert-server/)** is an open source server that can be added as an **Alert** source worldwide.

### GeoNames
> 🔐 **This source requires an API key.** [Register here](https://www.geonames.org/login)

**[GeoNames](https://www.geonames.org/)** provides multilingual search for place names of more than 11 million locations worldwide. This source can be enabled as a **Search** source after adding your API key.

### National Center for Disaster Reduction
**[National Science & Technology Center for Disaster Reduction](https://www.ncdr.nat.gov.tw/)** (NCDR) can be added as an **Alert** source for Taiwan.

### National Centers for Environmental Information
> NCEI is available for saved locations for the time being. It will be made available to current location in a future release.

**[National Centers for Environmental Information](https://www.ncei.noaa.gov/)** (NCEI) provides Global Summary of the Month (GSOM), a global climatological database that can be added as a **Temperature normals** source worldwide.

### National Land Survey and Mapping Center
**[National Land Survey and Mapping Center](https://www.nlsc.gov.tw/)** (NLSC) can be added as an **Address lookup** source for Taiwan.

### Nominatim
**[Nominatim](https://nominatim.org/)** can provide the address lookup feature, using OpenStreetMap data.

### Recosanté
> **April 2025 update:** Recosanté is temporarily not producing any data

**[Recosanté](https://recosante.beta.gouv.fr/)** can be added as a **Pollen** source for France. Only a pollen level is available, and not the concentration. Since it is sourcing its info from Atmo France which has concentration available, it’s best to use Atmo France source directly whenever possible.

### WMO Severe Weather
The **[WMO Severe Weather Information Centre](https://severeweather.wmo.int/)** is World Meteorological Organisation’s central repository of current and upcoming weather warnings from more than 130 countries and territories worldwide. This source can be added as an **Alert** source, which is particularly useful if the selected national source for your location does not provide Alert information.

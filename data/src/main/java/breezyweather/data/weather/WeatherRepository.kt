/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package breezyweather.data.weather

import breezyweather.data.DatabaseHandler
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.reference.Month
import java.util.Date

class WeatherRepository(
    private val handler: DatabaseHandler,
) {

    suspend fun getWeatherByLocationId(
        locationFormattedId: String,
        withDaily: Boolean = true,
        withHourly: Boolean = true,
        withMinutely: Boolean = true,
        withAlerts: Boolean = true,
        withNormals: Boolean = true,
    ): Weather? {
        val weather = handler.awaitOneOrNull {
            weathersQueries.getWeatherByLocationId(locationFormattedId, WeatherMapper::mapWeather)
        }

        return if (withDaily || withHourly || withMinutely || withAlerts) {
            weather?.copy(
                dailyForecast = if (withDaily) {
                    getDailyListByLocationId(locationFormattedId)
                } else {
                    emptyList()
                },
                hourlyForecast = if (withHourly) {
                    getHourlyListByLocationId(locationFormattedId)
                } else {
                    emptyList()
                },
                minutelyForecast = if (withMinutely) {
                    getMinutelyListByLocationId(locationFormattedId)
                } else {
                    emptyList()
                },
                alertList = if (withAlerts) {
                    getAlertListByLocationId(locationFormattedId)
                } else {
                    emptyList()
                },
                normals = if (withNormals) {
                    getNormalsByLocationId(locationFormattedId)
                } else {
                    emptyMap()
                }
            )
        } else {
            weather
        }
    }

    suspend fun getDailyListByLocationId(locationFormattedId: String): List<Daily> {
        return handler.awaitList {
            dailysQueries.getDailyListByLocationId(locationFormattedId, WeatherMapper::mapDaily)
        }
    }

    suspend fun getHourlyListByLocationId(locationFormattedId: String): List<Hourly> {
        return handler.awaitList {
            hourlysQueries.getHourlyListByLocationId(locationFormattedId, WeatherMapper::mapHourly)
        }
    }

    suspend fun getMinutelyListByLocationId(locationFormattedId: String): List<Minutely> {
        return handler.awaitList {
            minutelysQueries.getMinutelyListByLocationId(locationFormattedId, WeatherMapper::mapMinutely)
        }
    }

    suspend fun getAlertListByLocationId(locationFormattedId: String): List<Alert> {
        return handler.awaitList {
            alertsQueries.getAlertListByLocationId(locationFormattedId, WeatherMapper::mapAlert)
        }
    }

    suspend fun getNormalsByLocationId(locationFormattedId: String): Map<Month, Normals> {
        return handler.awaitList {
            normalsQueries.getNormalsByLocationId(locationFormattedId)
        }.associate {
            Month.of(it.month.toInt()) to Normals(
                daytimeTemperature = it.temperature_max_average,
                nighttimeTemperature = it.temperature_min_average
            )
        }
    }

    suspend fun getCurrentAlertsByLocationId(locationFormattedId: String): List<Alert> {
        return handler.awaitList {
            alertsQueries.getCurrentAlertsByLocationId(
                locationFormattedId,
                Date().time,
                WeatherMapper::mapAlert
            )
        }
    }

    suspend fun insert(location: Location, weather: Weather) {
        handler.await(inTransaction = true) {
            // 1. Save Weather (will replace if necessary)
            weathersQueries.insert(
                // base
                locationFormattedId = location.formattedId,
                refreshTime = weather.base.refreshTime?.time,
                mainUpdateTime = weather.base.forecastUpdateTime?.time,
                currentUpdateTime = weather.base.currentUpdateTime?.time,
                airQualityUpdateTime = weather.base.airQualityUpdateTime?.time,
                pollenUpdateTime = weather.base.pollenUpdateTime?.time,
                minutelyUpdateTime = weather.base.minutelyUpdateTime?.time,
                alertsUpdateTime = weather.base.alertsUpdateTime?.time,
                normalsUpdateTime = weather.base.normalsUpdateTime?.time,
                normalsUpdateLatitude = weather.base.normalsUpdateLatitude,
                normalsUpdateLongitude = weather.base.normalsUpdateLongitude,

                // current
                weatherText = weather.current?.weatherText,
                weatherCode = weather.current?.weatherCode,

                temperature = weather.current?.temperature?.temperature,
                temperatureSourceFeelsLike = weather.current?.temperature?.sourceFeelsLike,
                temperatureApparent = weather.current?.temperature?.computedApparent,
                temperatureWindChill = weather.current?.temperature?.computedWindChill,
                humidex = weather.current?.temperature?.computedHumidex,

                windDegree = weather.current?.wind?.degree,
                windSpeed = weather.current?.wind?.speed,
                windGusts = weather.current?.wind?.gusts,

                uvIndex = weather.current?.uV?.index,

                pm25 = weather.current?.airQuality?.pM25,
                pm10 = weather.current?.airQuality?.pM10,
                so2 = weather.current?.airQuality?.sO2,
                no2 = weather.current?.airQuality?.nO2,
                o3 = weather.current?.airQuality?.o3,
                co = weather.current?.airQuality?.cO,

                relativeHumidity = weather.current?.relativeHumidity,
                dewPoint = weather.current?.dewPoint,
                pressure = weather.current?.pressure,
                cloudCover = weather.current?.cloudCover,
                visibility = weather.current?.visibility,
                ceiling = weather.current?.ceiling,
                dailyForecast = weather.current?.dailyForecast,
                hourlyForecast = weather.current?.hourlyForecast
            )

            // 2. Save daily (delete first, then re-add)
            dailysQueries.deleteDailyListForLocationId(location.formattedId)
            weather.dailyForecast.forEach { daily ->
                dailysQueries.insert(
                    locationFormattedId = location.formattedId,
                    date = daily.date.time,

                    // daytime.
                    daytimeWeatherText = daily.day?.weatherText,
                    daytimeweatherSummary = daily.day?.weatherSummary,
                    daytimeWeatherCode = daily.day?.weatherCode,

                    daytimeTemperature = daily.day?.temperature?.temperature,
                    daytimeTemperatureSourceFeelsLike = daily.day?.temperature?.sourceFeelsLike,
                    daytimeTemperatureApparent = daily.day?.temperature?.computedApparent,
                    daytimeTemperatureWindChill = daily.day?.temperature?.computedWindChill,
                    daytimeHumidex = daily.day?.temperature?.computedHumidex,

                    daytimeTotalPrecipitation = daily.day?.precipitation?.total,
                    daytimeThunderstormPrecipitation = daily.day?.precipitation?.thunderstorm,
                    daytimeRainPrecipitation = daily.day?.precipitation?.rain,
                    daytimeSnowPrecipitation = daily.day?.precipitation?.snow,
                    daytimeIcePrecipitation = daily.day?.precipitation?.ice,

                    daytimeTotalPrecipitationProbability = daily.day?.precipitationProbability?.total,
                    daytimeThunderstormPrecipitationProbability = daily.day?.precipitationProbability?.thunderstorm,
                    daytimeRainPrecipitationProbability = daily.day?.precipitationProbability?.rain,
                    daytimeSnowPrecipitationProbability = daily.day?.precipitationProbability?.snow,
                    daytimeIcePrecipitationProbability = daily.day?.precipitationProbability?.ice,

                    daytimeTotalPrecipitationDuration = daily.day?.precipitationDuration?.total,
                    daytimeThunderstormPrecipitationDuration = daily.day?.precipitationDuration?.thunderstorm,
                    daytimeRainPrecipitationDuration = daily.day?.precipitationDuration?.rain,
                    daytimeSnowPrecipitationDuration = daily.day?.precipitationDuration?.snow,
                    daytimeIcePrecipitationDuration = daily.day?.precipitationDuration?.ice,

                    daytimeWindDegree = daily.day?.wind?.degree,
                    daytimeWindSpeed = daily.day?.wind?.speed,
                    daytimeWindGusts = daily.day?.wind?.gusts,

                    // nighttime.
                    nighttimeWeatherText = daily.night?.weatherText,
                    nighttimeweatherSummary = daily.night?.weatherSummary,
                    nighttimeWeatherCode = daily.night?.weatherCode,

                    nighttimeTemperature = daily.night?.temperature?.temperature,
                    nighttimeTemperatureSourceFeelsLike = daily.night?.temperature?.sourceFeelsLike,
                    nighttimeTemperatureApparent = daily.night?.temperature?.computedApparent,
                    nighttimeTemperatureWindChill = daily.night?.temperature?.computedWindChill,
                    nighttimeHumidex = daily.night?.temperature?.computedHumidex,

                    nighttimeTotalPrecipitation = daily.night?.precipitation?.total,
                    nighttimeThunderstormPrecipitation = daily.night?.precipitation?.thunderstorm,
                    nighttimeRainPrecipitation = daily.night?.precipitation?.rain,
                    nighttimeSnowPrecipitation = daily.night?.precipitation?.snow,
                    nighttimeIcePrecipitation = daily.night?.precipitation?.ice,

                    nighttimeTotalPrecipitationProbability = daily.night?.precipitationProbability?.total,
                    nighttimeThunderstormPrecipitationProbability = daily.night?.precipitationProbability?.thunderstorm,
                    nighttimeRainPrecipitationProbability = daily.night?.precipitationProbability?.rain,
                    nighttimeSnowPrecipitationProbability = daily.night?.precipitationProbability?.snow,
                    nighttimeIcePrecipitationProbability = daily.night?.precipitationProbability?.ice,

                    nighttimeTotalPrecipitationDuration = daily.night?.precipitationDuration?.total,
                    nighttimeThunderstormPrecipitationDuration = daily.night?.precipitationDuration?.thunderstorm,
                    nighttimeRainPrecipitationDuration = daily.night?.precipitationDuration?.rain,
                    nighttimeSnowPrecipitationDuration = daily.night?.precipitationDuration?.snow,
                    nighttimeIcePrecipitationDuration = daily.night?.precipitationDuration?.ice,

                    nighttimeWindDegree = daily.night?.wind?.degree,
                    nighttimeWindSpeed = daily.night?.wind?.speed,
                    nighttimeWindGusts = daily.night?.wind?.gusts,

                    degreeDayHeating = daily.degreeDay?.heating,
                    degreeDayCooling = daily.degreeDay?.cooling,

                    // sun
                    sunRiseDate = daily.sun?.riseDate?.time,
                    sunSetDate = daily.sun?.setDate?.time,

                    // twilight
                    twilightRiseDate = daily.twilight?.riseDate?.time,
                    twilightSetDate = daily.twilight?.setDate?.time,

                    // moon
                    moonRiseDate = daily.moon?.riseDate?.time,
                    moonSetDate = daily.moon?.setDate?.time,

                    // moon phase
                    moonPhaseAngle = daily.moonPhase?.angle?.toLong(),

                    // aqi.
                    pm25 = daily.airQuality?.pM25,
                    pm10 = daily.airQuality?.pM10,
                    so2 = daily.airQuality?.sO2,
                    no2 = daily.airQuality?.nO2,
                    o3 = daily.airQuality?.o3,
                    co = daily.airQuality?.cO,

                    // pollen
                    alder = daily.pollen?.alder,
                    ash = daily.pollen?.ash,
                    birch = daily.pollen?.birch,
                    chestnut = daily.pollen?.chestnut,
                    cypress = daily.pollen?.cypress,
                    grass = daily.pollen?.grass,
                    hazel = daily.pollen?.hazel,
                    hornbeam = daily.pollen?.hornbeam,
                    linden = daily.pollen?.linden,
                    mold = daily.pollen?.mold,
                    mugwort = daily.pollen?.mugwort,
                    oak = daily.pollen?.oak,
                    olive = daily.pollen?.olive,
                    plane = daily.pollen?.plane,
                    plantain = daily.pollen?.plantain,
                    poplar = daily.pollen?.poplar,
                    ragweed = daily.pollen?.ragweed,
                    sorrel = daily.pollen?.sorrel,
                    tree = daily.pollen?.tree,
                    urticaceae = daily.pollen?.urticaceae,
                    willow = daily.pollen?.willow,

                    // uv.
                    uvIndex = daily.uV?.index,

                    sunshineDuration = daily.sunshineDuration,

                    relativeHumidityAverage = daily.relativeHumidity?.average,
                    relativeHumidityMin = daily.relativeHumidity?.min,
                    relativeHumidityMax = daily.relativeHumidity?.max,

                    dewpointAverage = daily.dewPoint?.average,
                    dewpointMin = daily.dewPoint?.min,
                    dewpointMax = daily.dewPoint?.max,

                    pressureAverage = daily.pressure?.average,
                    pressureMin = daily.pressure?.min,
                    pressureMax = daily.pressure?.max,

                    cloudCoverAverage = daily.cloudCover?.average,
                    cloudCoverMin = daily.cloudCover?.min,
                    cloudCoverMax = daily.cloudCover?.max,

                    visibilityAverage = daily.visibility?.average,
                    visibilityMin = daily.visibility?.min,
                    visibilityMax = daily.visibility?.max
                )
            }

            // 3. Save hourly (delete first, then re-add)
            hourlysQueries.deleteHourlyListForLocationId(location.formattedId)
            weather.hourlyForecast.forEach { hourly ->
                hourlysQueries.insert(
                    locationFormattedId = location.formattedId,
                    date = hourly.date.time,
                    daylight = hourly.isDaylight,
                    weatherCode = hourly.weatherCode,
                    weatherText = hourly.weatherText,

                    temperature = hourly.temperature?.temperature,
                    temperatureSourceFeelsLike = hourly.temperature?.sourceFeelsLike,
                    temperatureApparent = hourly.temperature?.computedApparent,
                    temperatureWindChill = hourly.temperature?.computedWindChill,
                    humidex = hourly.temperature?.computedHumidex,

                    totalPrecipitation = hourly.precipitation?.total,
                    thunderstormPrecipitation = hourly.precipitation?.thunderstorm,
                    rainPrecipitation = hourly.precipitation?.rain,
                    snowPrecipitation = hourly.precipitation?.snow,
                    icePrecipitation = hourly.precipitation?.ice,

                    totalPrecipitationProbability = hourly.precipitationProbability?.total,
                    thunderstormPrecipitationProbability = hourly.precipitationProbability?.thunderstorm,
                    rainPrecipitationProbability = hourly.precipitationProbability?.rain,
                    snowPrecipitationProbability = hourly.precipitationProbability?.snow,
                    icePrecipitationProbability = hourly.precipitationProbability?.ice,

                    windDegree = hourly.wind?.degree,
                    windSpeed = hourly.wind?.speed,
                    windGusts = hourly.wind?.gusts,

                    // aqi.
                    pm25 = hourly.airQuality?.pM25,
                    pm10 = hourly.airQuality?.pM10,
                    so2 = hourly.airQuality?.sO2,
                    no2 = hourly.airQuality?.nO2,
                    o3 = hourly.airQuality?.o3,
                    co = hourly.airQuality?.cO,

                    // uv.
                    uvIndex = hourly.uV?.index,

                    // details
                    relativeHumidity = hourly.relativeHumidity,
                    dewPoint = hourly.dewPoint,
                    pressure = hourly.pressure,
                    cloudCover = hourly.cloudCover,
                    visibility = hourly.visibility
                )
            }

            // 4. Save minutely (delete first, then re-add)
            minutelysQueries.deleteMinutelyListForLocationId(location.formattedId)
            weather.minutelyForecast.forEach { minutely ->
                minutelysQueries.insert(
                    locationFormattedId = location.formattedId,
                    date = minutely.date.time,
                    minuteInterval = minutely.minuteInterval.toLong(),
                    precipitationIntensity = minutely.precipitationIntensity
                )
            }

            // 5. Save alerts (delete first, then re-add)
            alertsQueries.deleteAlertListForLocationId(location.formattedId)
            weather.alertList.forEach { alert ->
                alertsQueries.insert(
                    locationFormattedId = location.formattedId,
                    alertId = alert.alertId,
                    startDate = alert.startDate?.time,
                    endDate = alert.endDate?.time,
                    headline = alert.headline,
                    description = alert.description,
                    instruction = alert.instruction,
                    source = alert.source,
                    severity = alert.severity,
                    color = alert.color.toLong()
                )
            }

            // 6. Save normals (delete first, then re-add)
            normalsQueries.deleteNormalsForLocationId(location.formattedId)
            weather.normals.forEach { normals ->
                normalsQueries.insert(
                    locationFormattedId = location.formattedId,
                    month = normals.key.value.toLong(),
                    temperatureMaxAverage = normals.value.daytimeTemperature,
                    temperatureMinAverage = normals.value.nighttimeTemperature
                )
            }
        }
    }

    suspend fun deleteAllWeathers() {
        handler.await {
            weathersQueries.deleteAllWeathers()
        }
    }
}

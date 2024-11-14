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
import breezyweather.domain.weather.model.Weather
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
                mainUpdateTime = weather.base.mainUpdateTime?.time,
                currentUpdateTime = weather.base.currentUpdateTime?.time,
                airQualityUpdateTime = weather.base.airQualityUpdateTime?.time,
                pollenUpdateTime = weather.base.pollenUpdateTime?.time,
                minutelyUpdateTime = weather.base.minutelyUpdateTime?.time,
                alertsUpdateTime = weather.base.alertsUpdateTime?.time,
                normalsUpdateTime = weather.base.normalsUpdateTime?.time,

                // current
                weatherText = weather.current?.weatherText,
                weatherCode = weather.current?.weatherCode,

                temperature = weather.current?.temperature?.temperature,
                realFeelTemperature = weather.current?.temperature?.realFeelTemperature,
                realFeelShaderTemperature = weather.current?.temperature?.realFeelShaderTemperature,
                apparentTemperature = weather.current?.temperature?.apparentTemperature,
                windChillTemperature = weather.current?.temperature?.windChillTemperature,
                wetBulbTemperature = weather.current?.temperature?.wetBulbTemperature,

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
                cloudCover = weather.current?.cloudCover?.toLong(),
                visibility = weather.current?.visibility,
                ceiling = weather.current?.ceiling,
                dailyForecast = weather.current?.dailyForecast,
                hourlyForecast = weather.current?.hourlyForecast,

                // normals
                normalsMonth = weather.normals?.month?.toLong(),
                normalsDaytimeTemperature = weather.normals?.daytimeTemperature,
                normalsNighttimeTemperature = weather.normals?.nighttimeTemperature
            )

            // 2. Save daily (delete first, then re-add)
            dailysQueries.deleteDailyListForLocationId(location.formattedId)
            weather.dailyForecast.forEach { daily ->
                dailysQueries.insert(
                    locationFormattedId = location.formattedId,
                    date = daily.date.time,

                    // daytime.
                    daytimeWeatherText = daily.day?.weatherText,
                    daytimeWeatherPhase = daily.day?.weatherPhase,
                    daytimeWeatherCode = daily.day?.weatherCode,

                    daytimeTemperature = daily.day?.temperature?.temperature,
                    daytimeRealFeelTemperature = daily.day?.temperature?.realFeelTemperature,
                    daytimeRealFeelShaderTemperature = daily.day?.temperature?.realFeelShaderTemperature,
                    daytimeApparentTemperature = daily.day?.temperature?.apparentTemperature,
                    daytimeWindChillTemperature = daily.day?.temperature?.windChillTemperature,
                    daytimeWetBulbTemperature = daily.day?.temperature?.wetBulbTemperature,

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

                    daytimeCloudCover = daily.day?.cloudCover?.toLong(),

                    // nighttime.
                    nighttimeWeatherText = daily.night?.weatherText,
                    nighttimeWeatherPhase = daily.night?.weatherPhase,
                    nighttimeWeatherCode = daily.night?.weatherCode,

                    nighttimeTemperature = daily.night?.temperature?.temperature,
                    nighttimeRealFeelTemperature = daily.night?.temperature?.realFeelTemperature,
                    nighttimeRealFeelShaderTemperature = daily.night?.temperature?.realFeelShaderTemperature,
                    nighttimeApparentTemperature = daily.night?.temperature?.apparentTemperature,
                    nighttimeWindChillTemperature = daily.night?.temperature?.windChillTemperature,
                    nighttimeWetBulbTemperature = daily.night?.temperature?.wetBulbTemperature,

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

                    nighttimeCloudCover = daily.night?.cloudCover?.toLong(),

                    degreeDayHeating = daily.degreeDay?.heating,
                    degreeDayCooling = daily.degreeDay?.cooling,

                    // sun.
                    sunRiseDate = daily.sun?.riseDate?.time,
                    sunSetDate = daily.sun?.setDate?.time,

                    // moon.
                    moonRiseDate = daily.moon?.riseDate?.time,
                    moonSetDate = daily.moon?.setDate?.time,

                    // moon phase.
                    moonPhaseAngle = daily.moonPhase?.angle?.toLong(),

                    // aqi.
                    pm25 = daily.airQuality?.pM25,
                    pm10 = daily.airQuality?.pM10,
                    so2 = daily.airQuality?.sO2,
                    no2 = daily.airQuality?.nO2,
                    o3 = daily.airQuality?.o3,
                    co = daily.airQuality?.cO,

                    // pollen
                    alder = daily.pollen?.alder?.toLong(),
                    ash = daily.pollen?.ash?.toLong(),
                    birch = daily.pollen?.birch?.toLong(),
                    chestnut = daily.pollen?.chestnut?.toLong(),
                    cypress = daily.pollen?.cypress?.toLong(),
                    grass = daily.pollen?.grass?.toLong(),
                    hazel = daily.pollen?.hazel?.toLong(),
                    hornbeam = daily.pollen?.hornbeam?.toLong(),
                    linden = daily.pollen?.linden?.toLong(),
                    mold = daily.pollen?.mold?.toLong(),
                    mugwort = daily.pollen?.mugwort?.toLong(),
                    oak = daily.pollen?.oak?.toLong(),
                    olive = daily.pollen?.olive?.toLong(),
                    plane = daily.pollen?.plane?.toLong(),
                    plantain = daily.pollen?.plantain?.toLong(),
                    poplar = daily.pollen?.poplar?.toLong(),
                    ragweed = daily.pollen?.ragweed?.toLong(),
                    sorrel = daily.pollen?.sorrel?.toLong(),
                    tree = daily.pollen?.tree?.toLong(),
                    urticaceae = daily.pollen?.urticaceae?.toLong(),
                    willow = daily.pollen?.willow?.toLong(),

                    // uv.
                    uvIndex = daily.uV?.index,

                    sunshineDuration = daily.sunshineDuration
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
                    realFeelTemperature = hourly.temperature?.realFeelTemperature,
                    realFeelShaderTemperature = hourly.temperature?.realFeelShaderTemperature,
                    apparentTemperature = hourly.temperature?.apparentTemperature,
                    windChillTemperature = hourly.temperature?.windChillTemperature,
                    wetBulbTemperature = hourly.temperature?.wetBulbTemperature,

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
                    cloudCover = hourly.cloudCover?.toLong(),
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
        }
    }
}

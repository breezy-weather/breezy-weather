package org.breezyweather.sources.qweather

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.sources.qweather.json.QWeatherAlertsResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentAQIResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherDailyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherHourlyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherLocationProperties
import org.breezyweather.sources.qweather.json.QWeatherMinutelyPrecipitationResult
import java.util.TimeZone

fun convert(
    result: QWeatherLocationProperties
): Location {
    return Location(
        cityId = if (result.type == "city") result.id else null,
        city = result.name,
        country = result.country,
        latitude = result.lat,
        longitude = result.lon,
        province = result.adm1,
        district = result.adm2,
        timeZone = result.tz
    )
}

fun convert(
    currentweather: QWeatherCurrentWeatherResult,
    currentaqi: QWeatherCurrentAQIResult,
    alerts: QWeatherAlertsResult,
    hourlyweather: QWeatherHourlyWeatherResult,
    dailyweather: QWeatherDailyWeatherResult,
    minutelyPrecipitationResult: QWeatherMinutelyPrecipitationResult
): WeatherWrapper {
    return WeatherWrapper(
        current = currentweather.now?.let { now ->
            Current(
                weatherText = now.text,
                weatherCode = getWeatherCode(now.icon),
                temperature = Temperature(
                    temperature = now.temp?.toDoubleOrNull(),
                    realFeelTemperature = now.feelsLike?.toDoubleOrNull()
                ),
                wind = Wind(
                    speed = now.windSpeed?.toDoubleOrNull(),
                    degree = now.wind360?.toDoubleOrNull()
                ),
                airQuality = currentaqi.now?.let {
                    AirQuality(
                        pM25 = it.pm2p5?.toDoubleOrNull(),
                        pM10 = it.pm10?.toDoubleOrNull(),
                        o3 = it.o3?.toDoubleOrNull(),
                        nO2 = it.no2?.toDoubleOrNull(),
                        sO2 = it.so2?.toDoubleOrNull(),
                        cO = it.co?.toDoubleOrNull()
                    )
                },
                relativeHumidity = now.humidity?.toDoubleOrNull(),
                dewPoint = now.dew?.toDoubleOrNull(),
                pressure = now.pressure?.toDoubleOrNull(),
                visibility = now.vis?.toDoubleOrNull(),
                cloudCover = now.cloud?.toIntOrNull(),
                hourlyForecast = minutelyPrecipitationResult.summary
            )
        },
        hourlyForecast = hourlyweather.hourly?.map {
            HourlyWrapper(
                date = it.fxTime!!,
                weatherText = it.text,
                weatherCode = getWeatherCode(it.icon),
                temperature = Temperature(
                    temperature = it.temp?.toDoubleOrNull(),
                    realFeelTemperature = it.feelsLike?.toDoubleOrNull(),
                ),
                wind = Wind(
                    speed = it.windSpeed?.toDoubleOrNull(),
                    degree = it.wind360?.toDoubleOrNull(),
                ),
                relativeHumidity = it.humidity?.toDoubleOrNull(),
                dewPoint = it.dew?.toDoubleOrNull(),
                pressure = it.pressure?.toDoubleOrNull(),
                visibility = it.vis?.toDoubleOrNull()
            )
        },
        dailyForecast = dailyweather.daily?.map {
            Daily(
                date = it.fxDate,
                day = HalfDay(
                    weatherText = it.textDay,
                    weatherPhase = it.textDay,
                    weatherCode = getWeatherCode(it.iconDay),
                    temperature = Temperature(
                        temperature = it.tempMax?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        speed = it.windSpeedDay?.toDoubleOrNull(),
                        degree = it.wind360Day?.toDoubleOrNull()
                    ),
                ),
                night = HalfDay(
                    weatherText = it.textNight,
                    weatherPhase = it.textNight,
                    weatherCode = getWeatherCode(it.iconNight),
                    temperature = Temperature(
                        temperature = it.tempMin?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        speed = it.windSpeedNight?.toDoubleOrNull(),
                        degree = it.wind360Night?.toDoubleOrNull()
                    ),
                ),
                degreeDay = DegreeDay(
                    heating = it.tempMax?.toDoubleOrNull(),
                    cooling = it.tempMin?.toDoubleOrNull()
                )
            )
        },
        minutelyForecast = minutelyPrecipitationResult.minutely?.map {
            Minutely(
                date = it.fxTime,
                minuteInterval = 5,
                precipitationIntensity = it.precip?.toDoubleOrNull()
            )
        },
        alertList = alerts.warning?.map {
            Alert(
                alertId = it.id,
                headline = it.title,
                description = it.text,
                severity = 2,
                startDate = it.startTime,
                endDate = it.endTime
            )
        }
    )
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    if (icon.isNullOrEmpty()) return null
    return when (icon) {
        "100", "150" -> WeatherCode.PARTLY_CLOUDY
        "102", "103", "152", "153" -> WeatherCode.CLOUDY
        "101", "104", "151" -> WeatherCode.RAIN
        "300", "301", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318", "350", "351", "399" -> WeatherCode.SNOW
        "400", "401", "402", "403", "407", "408", "409", "410", "457", "499" -> WeatherCode.SLEET
        "404", "405", "406", "456" -> WeatherCode.HAIL
        "304" -> WeatherCode.FOG
        "500", "501", "509", "510", "514" -> WeatherCode.HAZE
        "502", "503", "504", "507", "508", "511", "512", "513" -> WeatherCode.THUNDER
        "302", "303" -> WeatherCode.WIND
        else -> WeatherCode.CLEAR
    }
}
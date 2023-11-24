package org.breezyweather.sources.qweather

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.sources.qweather.json.QWeatherAlertsResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentAQIResult
import org.breezyweather.sources.qweather.json.QWeatherCurrentWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherDailyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherHourlyWeatherResult
import org.breezyweather.sources.qweather.json.QWeatherLocationProperties
import org.breezyweather.sources.qweather.json.QWeatherMinutelyPrecipitationResult
import org.breezyweather.theme.weatherView.WeatherViewController.getWeatherCode
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
        timeZone = TimeZone.getTimeZone(result.tz)
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
        current = Current(
            weatherText = currentweather.now.text,
            weatherCode = getWeatherCode(getweathercodenum(currentweather.now.icon)),
            temperature = Temperature(
                temperature = currentweather.now.temp.toFloat(),
                realFeelTemperature = currentweather.now.feelsLike?.toFloat(),
            ),
            wind = Wind(
                speed = currentweather.now.windSpeed.toFloat(),
                degree = currentweather.now.wind360.toFloat(),
            ),
            airQuality = AirQuality(
                pM25 = currentaqi.now.pm2p5.toFloat(),
                pM10 = currentaqi.now.pm10.toFloat(),
                o3 = currentaqi.now.o3.toFloat(),
                nO2 = currentaqi.now.no2.toFloat(),
                sO2 = currentaqi.now.so2.toFloat(),
                cO = currentaqi.now.co.toFloat(),
            ),
            relativeHumidity = currentweather.now.humidity.toFloat(),
            dewPoint = currentweather.now.dew?.toFloat(),
            pressure = currentweather.now.pressure.toFloat(),
            visibility = currentweather.now.vis?.toFloat(),
            cloudCover = currentweather.now.cloud?.toFloat()?.toInt(),
            hourlyForecast = minutelyPrecipitationResult.summary
        ),
        hourlyForecast = hourlyweather.hourly.map {
            HourlyWrapper(
                date = it.fxTime!!,
                weatherText = it.text,
                weatherCode = getWeatherCode(getweathercodenum(it.icon)),
                temperature = Temperature(
                    temperature = it.temp.toFloat(),
                    realFeelTemperature = it.feelsLike?.toFloat(),
                ),
                wind = Wind(
                    speed = it.windSpeed.toFloat(),
                    degree = it.wind360.toFloat(),
                ),
                relativeHumidity = it.humidity.toFloat(),
                dewPoint = it.dew?.toFloat(),
                pressure = it.pressure.toFloat(),
                visibility = it.vis?.toFloat(),
            )
        },
        dailyForecast = dailyweather.daily.map {
            Daily(
                date = it.fxDate,
                day = HalfDay(
                    weatherText = it.textDay,
                    weatherPhase = it.textDay,
                    weatherCode = getWeatherCode(getweathercodenum(it.iconDay)),
                    temperature = Temperature(
                        temperature = it.tempMax.toFloat(),
                    ),
                    wind = Wind(
                        speed = it.windSpeedDay.toFloat(),
                        degree = it.wind360Day.toFloat(),
                    ),
                ),
                night = HalfDay(
                    weatherText = it.textNight,
                    weatherPhase = it.textNight,
                    weatherCode = getWeatherCode(getweathercodenum(it.iconNight)),
                    temperature = Temperature(
                        temperature = it.tempMin.toFloat(),
                    ),
                    wind = Wind(
                        speed = it.windSpeedNight.toFloat(),
                        degree = it.wind360Night.toFloat(),
                    ),
                ),
                degreeDay = DegreeDay(
                    heating = it.tempMax.toFloat(),
                    cooling = it.tempMin.toFloat(),
                ),
            )
        },
        minutelyForecast = minutelyPrecipitationResult.minutely.map {
            Minutely(
                date = it.fxTime,
                minuteInterval = 5,
                precipitationIntensity = it.precip.toDouble(),
            )
        },
        alertList = alerts.warning?.map {
            Alert(
                alertId = it.id,
                description = it.title,
                content = it.text,
                priority = 1,
                startDate = it.startTime,
                endDate = it.endTime,
            )
        },
    )
}

private fun getweathercodenum(icon: String?): Int {
    if (icon.isNullOrEmpty()) return 0
    return when (icon) {
        "100", "150" -> 1
        "102", "103", "152", "153" -> 2
        "101", "104", "151" -> 3
        "300", "301", "305", "306", "307", "308", "309", "310", "311", "312", "313", "314", "315", "316", "317", "318", "350", "351", "399" -> 4
        "400", "401", "402", "403", "407", "408", "409", "410", "457", "499" -> 5
        "404", "405", "406", "456" -> 6
        "304" -> 7
        "500", "501", "509", "510", "514" -> 8
        "502", "503", "504", "507", "508", "511", "512", "513" -> 9
        "302", "303" -> 11
        else -> 0
    }
}
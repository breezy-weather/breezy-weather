package org.breezyweather.db.generators

import io.objectbox.BoxStore
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.HistoryEntity
import org.breezyweather.db.entities.WeatherEntity

fun generate(location: Location, weather: Weather): WeatherEntity {
    val entity = WeatherEntity()

    // base.
    entity.cityId = weather.base.cityId
    entity.weatherSource = WeatherSourceConverter().convertToDatabaseValue(location.weatherSource)
    entity.publishDate = weather.base.publishDate
    entity.updateDate = weather.base.updateDate

    // current
    entity.weatherText = weather.current?.weatherText
    entity.weatherCode = weather.current?.weatherCode

    entity.temperature = weather.current?.temperature?.temperature
    entity.realFeelTemperature = weather.current?.temperature?.realFeelTemperature
    entity.realFeelShaderTemperature = weather.current?.temperature?.realFeelShaderTemperature
    entity.apparentTemperature = weather.current?.temperature?.apparentTemperature
    entity.windChillTemperature = weather.current?.temperature?.windChillTemperature
    entity.wetBulbTemperature = weather.current?.temperature?.wetBulbTemperature
    entity.degreeDayTemperature = weather.current?.temperature?.degreeDayTemperature

    entity.windDirection = weather.current?.wind?.direction
    entity.windDegree = if (weather.current?.wind?.degree != null
        && (weather.current.wind.degree.degree == null || weather.current.wind.degree.degree !in 0F..360F)
        && !weather.current.wind.degree.isNoDirection) null else weather.current?.wind?.degree
    entity.windSpeed = weather.current?.wind?.speed
    entity.windLevel = weather.current?.wind?.level

    entity.uvIndex = weather.current?.uV?.index
    entity.uvLevel = weather.current?.uV?.level
    entity.uvDescription = weather.current?.uV?.description

    entity.pm25 = weather.current?.airQuality?.pM25
    entity.pm10 = weather.current?.airQuality?.pM10
    entity.so2 = weather.current?.airQuality?.sO2
    entity.no2 = weather.current?.airQuality?.nO2
    entity.o3 = weather.current?.airQuality?.o3
    entity.co = weather.current?.airQuality?.cO

    entity.relativeHumidity = weather.current?.relativeHumidity
    entity.pressure = weather.current?.pressure
    entity.visibility = weather.current?.visibility
    entity.dewPoint = weather.current?.dewPoint
    entity.cloudCover = weather.current?.cloudCover
    entity.ceiling = weather.current?.ceiling
    entity.dailyForecast = weather.current?.dailyForecast
    entity.hourlyForecast = weather.current?.hourlyForecast

    return entity
}

fun generate(
    weatherEntity: WeatherEntity?,
    historyEntity: HistoryEntity?,
    boxStore: BoxStore?
): Weather? {
    return if (weatherEntity == null) {
        null
    } else Weather(
        Base(
            weatherEntity.cityId,
            weatherEntity.publishDate,
            weatherEntity.updateDate
        ),
        Current(
            weatherEntity.weatherText, weatherEntity.weatherCode,
            Temperature(
                weatherEntity.temperature,
                weatherEntity.realFeelTemperature, weatherEntity.realFeelShaderTemperature,
                weatherEntity.apparentTemperature,
                weatherEntity.windChillTemperature,
                weatherEntity.wetBulbTemperature,
                weatherEntity.degreeDayTemperature
            ),
            Wind(
                weatherEntity.windDirection,
                weatherEntity.windDegree,
                weatherEntity.windSpeed,
                weatherEntity.windLevel
            ),
            UV(
                weatherEntity.uvIndex,
                weatherEntity.uvLevel,
                weatherEntity.uvDescription
            ),
            AirQuality(
                weatherEntity.pm25,
                weatherEntity.pm10,
                weatherEntity.so2,
                weatherEntity.no2,
                weatherEntity.o3,
                weatherEntity.co
            ),
            weatherEntity.relativeHumidity,
            weatherEntity.pressure,
            weatherEntity.visibility,
            weatherEntity.dewPoint,
            weatherEntity.cloudCover,
            weatherEntity.ceiling,
            weatherEntity.dailyForecast,
            weatherEntity.hourlyForecast
        ),
        generate(historyEntity),
        generate(weatherEntity.getDailyEntityList(boxStore)),
        generateModuleList(weatherEntity.getHourlyEntityList(boxStore)),
        MinutelyEntityGenerator.generate(weatherEntity.getMinutelyEntityList(boxStore)),
        AlertEntityGenerator.generate(weatherEntity.getAlertEntityList(boxStore))
    )
}

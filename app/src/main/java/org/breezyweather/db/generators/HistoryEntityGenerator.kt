package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.db.converters.WeatherSourceConverter
import org.breezyweather.db.entities.HistoryEntity

fun generate(cityId: String, source: WeatherSource, history: History): HistoryEntity {
    return HistoryEntity(
        cityId = cityId,
        weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
        date = history.date,
        daytimeTemperature = history.daytimeTemperature,
        nighttimeTemperature = history.nighttimeTemperature
    )
}

fun generate(cityId: String, source: WeatherSource, weather: Weather): HistoryEntity {
    return HistoryEntity(
        cityId = cityId,
        weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
        date = weather.base.publishDate,
        daytimeTemperature = weather.dailyForecast.getOrNull(0)?.day?.temperature?.temperature,
        nighttimeTemperature = weather.dailyForecast.getOrNull(0)?.night?.temperature?.temperature
    )
}

fun generate(entity: HistoryEntity?): History? {
    return if (entity == null) {
        null
    } else History(
        entity.date,
        entity.daytimeTemperature,
        entity.nighttimeTemperature
    )
}

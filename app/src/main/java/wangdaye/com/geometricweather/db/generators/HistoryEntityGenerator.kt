package wangdaye.com.geometricweather.db.generators

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.History
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.HistoryEntity

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

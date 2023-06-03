package wangdaye.com.geometricweather.db.generators

import io.objectbox.BoxStore
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality
import wangdaye.com.geometricweather.common.basic.models.weather.Base
import wangdaye.com.geometricweather.common.basic.models.weather.Current
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature
import wangdaye.com.geometricweather.common.basic.models.weather.UV
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.basic.models.weather.Wind
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.HistoryEntity
import wangdaye.com.geometricweather.db.entities.WeatherEntity

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

    entity.totalPrecipitation = weather.current?.precipitation?.total
    entity.thunderstormPrecipitation = weather.current?.precipitation?.thunderstorm
    entity.rainPrecipitation = weather.current?.precipitation?.rain
    entity.snowPrecipitation = weather.current?.precipitation?.snow
    entity.icePrecipitation = weather.current?.precipitation?.ice

    entity.windDirection = weather.current?.wind?.direction
    entity.windDegree = weather.current?.wind?.degree
    entity.windSpeed = weather.current?.wind?.speed
    entity.windLevel = weather.current?.wind?.level

    entity.uvIndex = weather.current?.uV?.index
    entity.uvLevel = weather.current?.uV?.level
    entity.uvDescription = weather.current?.uV?.description

    entity.aqiIndex = weather.current?.airQuality?.aqiIndex
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
            Precipitation(
                weatherEntity.totalPrecipitation,
                weatherEntity.thunderstormPrecipitation,
                weatherEntity.rainPrecipitation,
                weatherEntity.snowPrecipitation,
                weatherEntity.icePrecipitation
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
                weatherEntity.aqiIndex,
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

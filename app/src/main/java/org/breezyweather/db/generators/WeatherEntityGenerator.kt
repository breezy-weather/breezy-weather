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
import org.breezyweather.db.entities.HistoryEntity
import org.breezyweather.db.entities.WeatherEntity

object WeatherEntityGenerator {
    fun generate(location: Location, weather: Weather): WeatherEntity {
        return WeatherEntity(

            // base.
            cityId = weather.base.cityId,
            weatherSource = location.weatherSource,
            publishDate = weather.base.publishDate,
            updateDate = weather.base.updateDate,

            // current
            weatherText = weather.current?.weatherText,
            weatherCode = weather.current?.weatherCode,

            temperature = weather.current?.temperature?.temperature,
            realFeelTemperature = weather.current?.temperature?.realFeelTemperature,
            realFeelShaderTemperature = weather.current?.temperature?.realFeelShaderTemperature,
            apparentTemperature = weather.current?.temperature?.apparentTemperature,
            windChillTemperature = weather.current?.temperature?.windChillTemperature,
            wetBulbTemperature = weather.current?.temperature?.wetBulbTemperature,

            windDirection = weather.current?.wind?.direction,
            windDegree = if (weather.current?.wind?.degree != null
                && (weather.current.wind.degree.degree == null || weather.current.wind.degree.degree !in 0F..360F)
                && !weather.current.wind.degree.isNoDirection) null else weather.current?.wind?.degree,
            windSpeed = weather.current?.wind?.speed,
            windLevel = weather.current?.wind?.level,

            uvIndex = weather.current?.uV?.index,
            uvLevel = weather.current?.uV?.level,
            uvDescription = weather.current?.uV?.description,

            pm25 = weather.current?.airQuality?.pM25,
            pm10 = weather.current?.airQuality?.pM10,
            so2 = weather.current?.airQuality?.sO2,
            no2 = weather.current?.airQuality?.nO2,
            o3 = weather.current?.airQuality?.o3,
            co = weather.current?.airQuality?.cO,

            relativeHumidity = weather.current?.relativeHumidity,
            pressure = weather.current?.pressure,
            visibility = weather.current?.visibility,
            dewPoint = weather.current?.dewPoint,
            cloudCover = weather.current?.cloudCover,
            ceiling = weather.current?.ceiling,
            dailyForecast = weather.current?.dailyForecast,
            hourlyForecast = weather.current?.hourlyForecast
        )
    }

    fun generate(
        weatherEntity: WeatherEntity?,
        historyEntity: HistoryEntity?,
        boxStore: BoxStore
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
                weatherEntity.weatherText,
                weatherEntity.weatherCode,
                Temperature(
                    weatherEntity.temperature,
                    weatherEntity.realFeelTemperature,
                    weatherEntity.realFeelShaderTemperature,
                    weatherEntity.apparentTemperature,
                    weatherEntity.windChillTemperature,
                    weatherEntity.wetBulbTemperature
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
            HistoryEntityGenerator.generate(historyEntity),
            DailyEntityGenerator.generate(weatherEntity.getDailyEntityList(boxStore)),
            HourlyEntityGenerator.generateModuleList(weatherEntity.getHourlyEntityList(boxStore)),
            MinutelyEntityGenerator.generate(weatherEntity.getMinutelyEntityList(boxStore)),
            AlertEntityGenerator.generate(weatherEntity.getAlertEntityList(boxStore))
        )
    }

}

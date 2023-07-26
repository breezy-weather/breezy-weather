package org.breezyweather.db.generators

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.db.entities.HourlyEntity

object HourlyEntityGenerator {
    fun generate(cityId: String, source: String, hourly: Hourly): HourlyEntity {
        return HourlyEntity(
            cityId = cityId,
            weatherSource = source,
            date = hourly.date,
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

    fun generateEntityList(cityId: String, source: String, hourlyList: List<Hourly>): List<HourlyEntity> {
        val entityList: MutableList<HourlyEntity> = ArrayList(hourlyList.size)
        for (hourly in hourlyList) {
            entityList.add(generate(cityId, source, hourly))
        }
        return entityList
    }

    fun generate(entity: HourlyEntity): Hourly {
        return Hourly(
            entity.date, entity.daylight,
            entity.weatherText, entity.weatherCode,
            Temperature(
                entity.temperature,
                entity.realFeelTemperature,
                entity.realFeelShaderTemperature,
                entity.apparentTemperature,
                entity.windChillTemperature,
                entity.wetBulbTemperature
            ),
            Precipitation(
                entity.totalPrecipitation,
                entity.thunderstormPrecipitation,
                entity.rainPrecipitation,
                entity.snowPrecipitation,
                entity.icePrecipitation
            ),
            PrecipitationProbability(
                entity.totalPrecipitationProbability,
                entity.thunderstormPrecipitationProbability,
                entity.rainPrecipitationProbability,
                entity.snowPrecipitationProbability,
                entity.icePrecipitationProbability
            ),
            Wind(
                entity.windDegree,
                entity.windSpeed
            ),
            AirQuality(
                entity.pm25,
                entity.pm10,
                entity.so2,
                entity.no2,
                entity.o3,
                entity.co
            ),
            UV(entity.uvIndex),
            entity.relativeHumidity,
            entity.dewPoint,
            entity.pressure,
            entity.cloudCover,
            entity.visibility
        )
    }

    fun generateModuleList(entityList: List<HourlyEntity>): List<Hourly> {
        val hourlyList: MutableList<Hourly> = ArrayList(entityList.size)
        for (entity in entityList) {
            hourlyList.add(generate(entity))
        }
        return hourlyList
    }
}

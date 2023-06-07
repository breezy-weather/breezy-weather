package wangdaye.com.geometricweather.db.generators

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature
import wangdaye.com.geometricweather.common.basic.models.weather.UV
import wangdaye.com.geometricweather.common.basic.models.weather.Wind
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.HourlyEntity

fun generate(cityId: String, source: WeatherSource, hourly: Hourly): HourlyEntity {
    return HourlyEntity(
        cityId = cityId,
        weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
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
        degreeDayTemperature = hourly.temperature?.degreeDayTemperature,

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

        windDirection = hourly.wind?.direction,
        windDegree = if (hourly.wind?.degree != null
            && (hourly.wind.degree.degree == null || hourly.wind.degree.degree !in 0F..360F)
            && !hourly.wind.degree.isNoDirection) null else hourly.wind?.degree,
        windSpeed = hourly.wind?.speed,
        windLevel = hourly.wind?.level,

        // aqi.
        pm25 = hourly.airQuality?.pM25,
        pm10 = hourly.airQuality?.pM10,
        so2 = hourly.airQuality?.sO2,
        no2 = hourly.airQuality?.nO2,
        o3 = hourly.airQuality?.o3,
        co = hourly.airQuality?.cO,

        // pollen?.
        grassIndex = hourly.pollen?.grassIndex,
        grassLevel = hourly.pollen?.grassLevel,
        grassDescription = hourly.pollen?.grassDescription,
        moldIndex = hourly.pollen?.moldIndex,
        moldLevel = hourly.pollen?.moldLevel,
        moldDescription = hourly.pollen?.moldDescription,
        ragweedIndex = hourly.pollen?.ragweedIndex,
        ragweedLevel = hourly.pollen?.ragweedLevel,
        ragweedDescription = hourly.pollen?.ragweedDescription,
        treeIndex = hourly.pollen?.treeIndex,
        treeLevel = hourly.pollen?.treeLevel,
        treeDescription = hourly.pollen?.treeDescription,

        // uv.
        uvIndex = hourly.uV?.index,
        uvLevel = hourly.uV?.level,
        uvDescription = hourly.uV?.description
    )
}

fun generateEntityList(cityId: String, source: WeatherSource, hourlyList: List<Hourly>): List<HourlyEntity> {
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
            entity.wetBulbTemperature,
            entity.degreeDayTemperature
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
            entity.windDirection,
            entity.windDegree,
            entity.windSpeed,
            entity.windLevel
        ),
        AirQuality(
            entity.pm25,
            entity.pm10,
            entity.so2,
            entity.no2,
            entity.o3,
            entity.co
        ),
        Pollen(
            entity.grassIndex, entity.grassLevel, entity.grassDescription,
            entity.moldIndex, entity.moldLevel, entity.moldDescription,
            entity.ragweedIndex, entity.ragweedLevel, entity.ragweedDescription,
            entity.treeIndex, entity.treeLevel, entity.treeDescription
        ),
        UV(entity.uvIndex, entity.uvLevel, entity.uvDescription)
    )
}

fun generateModuleList(entityList: List<HourlyEntity>): List<Hourly> {
    val hourlyList: MutableList<Hourly> = ArrayList(entityList.size)
    for (entity in entityList) {
        hourlyList.add(generate(entity))
    }
    return hourlyList
}
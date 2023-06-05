package wangdaye.com.geometricweather.db.generators

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.AirQuality
import wangdaye.com.geometricweather.common.basic.models.weather.Astro
import wangdaye.com.geometricweather.common.basic.models.weather.Daily
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay
import wangdaye.com.geometricweather.common.basic.models.weather.MoonPhase
import wangdaye.com.geometricweather.common.basic.models.weather.Pollen
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationDuration
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature
import wangdaye.com.geometricweather.common.basic.models.weather.UV
import wangdaye.com.geometricweather.common.basic.models.weather.Wind
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter
import wangdaye.com.geometricweather.db.entities.DailyEntity

fun generate(cityId: String, source: WeatherSource, daily: Daily): DailyEntity {
    return DailyEntity(
        cityId = cityId,
        weatherSource = WeatherSourceConverter().convertToDatabaseValue(source),
        date = daily.date,

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
        daytimeDegreeDayTemperature = daily.day?.temperature?.degreeDayTemperature,

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

        daytimeWindDirection = daily.day?.wind?.direction,
        daytimeWindDegree = if (daily.day?.wind?.degree != null
            && (daily.day.wind.degree.degree == null || daily.day.wind.degree.degree !in 0F..360F)
            && !daily.day.wind.degree.isNoDirection) null else daily.day?.wind?.degree,
        daytimeWindSpeed = daily.day?.wind?.speed,
        daytimeWindLevel = daily.day?.wind?.level,

        daytimeCloudCover = daily.day?.cloudCover,

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
        nighttimeDegreeDayTemperature = daily.night?.temperature?.degreeDayTemperature,

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

        nighttimeWindDirection = daily.night?.wind?.direction,
        nighttimeWindDegree = daily.night?.wind?.degree,
        nighttimeWindSpeed = daily.night?.wind?.speed,
        nighttimeWindLevel = daily.night?.wind?.level,

        nighttimeCloudCover = daily.night?.cloudCover,

        // sun.
        sunRiseDate = daily.sun?.riseDate,
        sunSetDate = daily.sun?.setDate,

        // moon.
        moonRiseDate = daily.moon?.riseDate,
        moonSetDate = daily.moon?.setDate,

        // moon phase.
        moonPhaseAngle = daily.moonPhase?.angle,
        moonPhaseDescription = daily.moonPhase?.description,

        // aqi.
        epaIndex = daily.airQuality?.epaIndex,
        meeIndex = daily.airQuality?.meeIndex,
        eeaIndex = daily.airQuality?.eeaIndex,
        pm25 = daily.airQuality?.pM25,
        pm10 = daily.airQuality?.pM10,
        so2 = daily.airQuality?.sO2,
        no2 = daily.airQuality?.nO2,
        o3 = daily.airQuality?.o3,
        co = daily.airQuality?.cO,

        // pollen.
        grassIndex = daily.pollen?.grassIndex,
        grassLevel = daily.pollen?.grassLevel,
        grassDescription = daily.pollen?.grassDescription,
        moldIndex = daily.pollen?.moldIndex,
        moldLevel = daily.pollen?.moldLevel,
        moldDescription = daily.pollen?.moldDescription,
        ragweedIndex = daily.pollen?.ragweedIndex,
        ragweedLevel = daily.pollen?.ragweedLevel,
        ragweedDescription = daily.pollen?.ragweedDescription,
        treeIndex = daily.pollen?.treeIndex,
        treeLevel = daily.pollen?.treeLevel,
        treeDescription = daily.pollen?.treeDescription,

        // uv.
        uvIndex = daily.uV?.index,
        uvLevel = daily.uV?.level,
        uvDescription = daily.uV?.description,

        hoursOfSun = daily.hoursOfSun
    )
}

fun generate(cityId: String, source: WeatherSource, dailyList: List<Daily>): List<DailyEntity> {
    val entityList: MutableList<DailyEntity> = ArrayList(dailyList.size)
    for (daily in dailyList) {
        entityList.add(generate(cityId, source, daily))
    }
    return entityList
}

fun generate(entity: DailyEntity): Daily {
    return Daily(
        entity.date,
        HalfDay(
            entity.daytimeWeatherText, entity.daytimeWeatherPhase, entity.daytimeWeatherCode,
            Temperature(
                entity.daytimeTemperature,
                entity.daytimeRealFeelTemperature,
                entity.daytimeRealFeelShaderTemperature,
                entity.daytimeApparentTemperature,
                entity.daytimeWindChillTemperature,
                entity.daytimeWetBulbTemperature,
                entity.daytimeDegreeDayTemperature
            ),
            Precipitation(
                entity.daytimeTotalPrecipitation,
                entity.daytimeThunderstormPrecipitation,
                entity.daytimeRainPrecipitation,
                entity.daytimeSnowPrecipitation,
                entity.daytimeIcePrecipitation
            ),
            PrecipitationProbability(
                entity.daytimeTotalPrecipitationProbability,
                entity.daytimeThunderstormPrecipitationProbability,
                entity.daytimeRainPrecipitationProbability,
                entity.daytimeSnowPrecipitationProbability,
                entity.daytimeIcePrecipitationProbability
            ),
            PrecipitationDuration(
                entity.daytimeTotalPrecipitationDuration,
                entity.daytimeThunderstormPrecipitationDuration,
                entity.daytimeRainPrecipitationDuration,
                entity.daytimeSnowPrecipitationDuration,
                entity.daytimeIcePrecipitationDuration
            ),
            Wind(
                entity.daytimeWindDirection,
                entity.daytimeWindDegree,
                entity.daytimeWindSpeed,
                entity.daytimeWindLevel
            ),
            entity.daytimeCloudCover
        ),
        HalfDay(
            entity.nighttimeWeatherText,
            entity.nighttimeWeatherPhase,
            entity.nighttimeWeatherCode,
            Temperature(
                entity.nighttimeTemperature,
                entity.nighttimeRealFeelTemperature,
                entity.nighttimeRealFeelShaderTemperature,
                entity.nighttimeApparentTemperature,
                entity.nighttimeWindChillTemperature,
                entity.nighttimeWetBulbTemperature,
                entity.nighttimeDegreeDayTemperature
            ),
            Precipitation(
                entity.nighttimeTotalPrecipitation,
                entity.nighttimeThunderstormPrecipitation,
                entity.nighttimeRainPrecipitation,
                entity.nighttimeSnowPrecipitation,
                entity.nighttimeIcePrecipitation
            ),
            PrecipitationProbability(
                entity.nighttimeTotalPrecipitationProbability,
                entity.nighttimeThunderstormPrecipitationProbability,
                entity.nighttimeRainPrecipitationProbability,
                entity.nighttimeSnowPrecipitationProbability,
                entity.nighttimeIcePrecipitationProbability
            ),
            PrecipitationDuration(
                entity.nighttimeTotalPrecipitationDuration,
                entity.nighttimeThunderstormPrecipitationDuration,
                entity.nighttimeRainPrecipitationDuration,
                entity.nighttimeSnowPrecipitationDuration,
                entity.nighttimeIcePrecipitationDuration
            ),
            Wind(
                entity.nighttimeWindDirection,
                entity.nighttimeWindDegree,
                entity.nighttimeWindSpeed,
                entity.nighttimeWindLevel
            ),
            entity.nighttimeCloudCover
        ),
        Astro(entity.sunRiseDate, entity.sunSetDate),
        Astro(entity.moonRiseDate, entity.moonSetDate),
        MoonPhase(entity.moonPhaseAngle, entity.moonPhaseDescription),
        AirQuality(
            entity.epaIndex,
            entity.meeIndex,
            entity.eeaIndex,
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
        UV(entity.uvIndex, entity.uvLevel, entity.uvDescription),
        entity.hoursOfSun
    )
}

fun generate(entityList: List<DailyEntity>): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(entityList.size)
    for (entity in entityList) {
        dailyList.add(generate(entity))
    }
    return dailyList
}
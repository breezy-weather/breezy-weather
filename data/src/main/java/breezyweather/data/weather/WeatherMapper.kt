package breezyweather.data.weather

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Base
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import java.util.Date

object WeatherMapper {

    fun mapWeather(
        refreshTime: Long?,
        mainUpdateTime: Long?,
        currentUpdateTime: Long?,
        airQualityUpdateTime: Long?,
        pollenUpdateTime: Long?,
        minutelyUpdateTime: Long?,
        alertsUpdateTime: Long?,
        normalsUpdateTime: Long?,
        weatherText: String?,
        weatherCode: WeatherCode?,
        temperature: Double?,
        realFeelTemperature: Double?,
        realFeelShaderTemperature: Double?,
        apparentTemperature: Double?,
        windChillTemperature: Double?,
        wetBulbTemperature: Double?,
        windDegree: Double?,
        windSpeed: Double?,
        windGusts: Double?,
        uvIndex: Double?,
        pm25: Double?,
        pm10: Double?,
        so2: Double?,
        no2: Double?,
        o3: Double?,
        co: Double?,
        relativeHumidity: Double?,
        dewPoint: Double?,
        pressure: Double?,
        visibility: Double?,
        cloudCover: Long?,
        ceiling: Double?,
        dailyForecast: String?,
        hourlyForecast: String?,
        normalsMonth: Long?,
        normalsDaytimeTemperature: Double?,
        normalsNighttimeTemperature: Double?,
    ): Weather = Weather(
        Base(
            refreshTime?.let { Date(it) },
            mainUpdateTime?.let { Date(it) },
            currentUpdateTime?.let { Date(it) },
            airQualityUpdateTime?.let { Date(it) },
            pollenUpdateTime?.let { Date(it) },
            minutelyUpdateTime?.let { Date(it) },
            alertsUpdateTime?.let { Date(it) },
            normalsUpdateTime?.let { Date(it) }
        ),
        Current(
            weatherText,
            weatherCode,
            Temperature(
                temperature,
                realFeelTemperature,
                realFeelShaderTemperature,
                apparentTemperature,
                windChillTemperature,
                wetBulbTemperature
            ),
            Wind(
                windDegree,
                windSpeed,
                windGusts
            ),
            UV(uvIndex),
            AirQuality(
                pm25,
                pm10,
                so2,
                no2,
                o3,
                co
            ),
            relativeHumidity,
            dewPoint,
            pressure,
            cloudCover?.toInt(),
            visibility,
            ceiling,
            dailyForecast,
            hourlyForecast
        ),
        Normals(
            normalsMonth?.toInt(),
            normalsDaytimeTemperature,
            normalsNighttimeTemperature
        )
    )

    fun mapDaily(
        date: Long,
        daytimeWeatherText: String?,
        daytimeWeatherPhase: String?,
        daytimeWeatherCode: WeatherCode?,
        daytimeTemperature: Double?,
        daytimeRealFeelTemperature: Double?,
        daytimeRealFeelShaderTemperature: Double?,
        daytimeApparentTemperature: Double?,
        daytimeWindChillTemperature: Double?,
        daytimeWetBulbTemperature: Double?,
        daytimeTotalPrecipitation: Double?,
        daytimeThunderstormPrecipitation: Double?,
        daytimeRainPrecipitation: Double?,
        daytimeSnowPrecipitation: Double?,
        daytimeIcePrecipitation: Double?,
        daytimeTotalPrecipitationProbability: Double?,
        daytimeThunderstormPrecipitationProbability: Double?,
        daytimeRainPrecipitationProbability: Double?,
        daytimeSnowPrecipitationProbability: Double?,
        daytimeIcePrecipitationProbability: Double?,
        daytimeTotalPrecipitationDuration: Double?,
        daytimeThunderstormPrecipitationDuration: Double?,
        daytimeRainPrecipitationDuration: Double?,
        daytimeSnowPrecipitationDuration: Double?,
        daytimeIcePrecipitationDuration: Double?,
        daytimeWindDegree: Double?,
        daytimeWindSpeed: Double?,
        daytimeWindGusts: Double?,
        daytimeCloudCover: Long?,
        nighttimeWeatherText: String?,
        nighttimeWeatherPhase: String?,
        nighttimeWeatherCode: WeatherCode?,
        nighttimeTemperature: Double?,
        nighttimeRealFeelTemperature: Double?,
        nighttimeRealFeelShaderTemperature: Double?,
        nighttimeApparentTemperature: Double?,
        nighttimeWindChillTemperature: Double?,
        nighttimeWetBulbTemperature: Double?,
        nighttimeTotalPrecipitation: Double?,
        nighttimeThunderstormPrecipitation: Double?,
        nighttimeRainPrecipitation: Double?,
        nighttimeSnowPrecipitation: Double?,
        nighttimeIcePrecipitation: Double?,
        nighttimeTotalPrecipitationProbability: Double?,
        nighttimeThunderstormPrecipitationProbability: Double?,
        nighttimeRainPrecipitationProbability: Double?,
        nighttimeSnowPrecipitationProbability: Double?,
        nighttimeIcePrecipitationProbability: Double?,
        nighttimeTotalPrecipitationDuration: Double?,
        nighttimeThunderstormPrecipitationDuration: Double?,
        nighttimeRainPrecipitationDuration: Double?,
        nighttimeSnowPrecipitationDuration: Double?,
        nighttimeIcePrecipitationDuration: Double?,
        nighttimeWindDegree: Double?,
        nighttimeWindSpeed: Double?,
        nighttimeWindGusts: Double?,
        nighttimeCloudCover: Long?,
        degreeDayHeating: Double?,
        degreeDayCooling: Double?,
        sunRiseDate: Long?,
        sunSetDate: Long?,
        moonRiseDate: Long?,
        moonSetDate: Long?,
        moonPhaseAngle: Long?,
        pm25: Double?,
        pm10: Double?,
        so2: Double?,
        no2: Double?,
        o3: Double?,
        co: Double?,
        alder: Long?,
        ash: Long?,
        birch: Long?,
        chestnut: Long?,
        cypress: Long?,
        grass: Long?,
        hazel: Long?,
        hornbeam: Long?,
        linden: Long?,
        mold: Long?,
        mugwort: Long?,
        oak: Long?,
        olive: Long?,
        plane: Long?,
        plantain: Long?,
        poplar: Long?,
        ragweed: Long?,
        sorrel: Long?,
        tree: Long?,
        urticaceae: Long?,
        willow: Long?,
        uvIndex: Double?,
        sunshineDuration: Double?,
    ): Daily = Daily(
        Date(date),
        HalfDay(
            daytimeWeatherText, daytimeWeatherPhase, daytimeWeatherCode,
            Temperature(
                daytimeTemperature,
                daytimeRealFeelTemperature,
                daytimeRealFeelShaderTemperature,
                daytimeApparentTemperature,
                daytimeWindChillTemperature,
                daytimeWetBulbTemperature
            ),
            Precipitation(
                daytimeTotalPrecipitation,
                daytimeThunderstormPrecipitation,
                daytimeRainPrecipitation,
                daytimeSnowPrecipitation,
                daytimeIcePrecipitation
            ),
            PrecipitationProbability(
                daytimeTotalPrecipitationProbability,
                daytimeThunderstormPrecipitationProbability,
                daytimeRainPrecipitationProbability,
                daytimeSnowPrecipitationProbability,
                daytimeIcePrecipitationProbability
            ),
            PrecipitationDuration(
                daytimeTotalPrecipitationDuration,
                daytimeThunderstormPrecipitationDuration,
                daytimeRainPrecipitationDuration,
                daytimeSnowPrecipitationDuration,
                daytimeIcePrecipitationDuration
            ),
            Wind(
                daytimeWindDegree,
                daytimeWindSpeed,
                daytimeWindGusts
            ),
            daytimeCloudCover?.toInt()
        ),
        HalfDay(
            nighttimeWeatherText,
            nighttimeWeatherPhase,
            nighttimeWeatherCode,
            Temperature(
                nighttimeTemperature,
                nighttimeRealFeelTemperature,
                nighttimeRealFeelShaderTemperature,
                nighttimeApparentTemperature,
                nighttimeWindChillTemperature,
                nighttimeWetBulbTemperature
            ),
            Precipitation(
                nighttimeTotalPrecipitation,
                nighttimeThunderstormPrecipitation,
                nighttimeRainPrecipitation,
                nighttimeSnowPrecipitation,
                nighttimeIcePrecipitation
            ),
            PrecipitationProbability(
                nighttimeTotalPrecipitationProbability,
                nighttimeThunderstormPrecipitationProbability,
                nighttimeRainPrecipitationProbability,
                nighttimeSnowPrecipitationProbability,
                nighttimeIcePrecipitationProbability
            ),
            PrecipitationDuration(
                nighttimeTotalPrecipitationDuration,
                nighttimeThunderstormPrecipitationDuration,
                nighttimeRainPrecipitationDuration,
                nighttimeSnowPrecipitationDuration,
                nighttimeIcePrecipitationDuration
            ),
            Wind(
                nighttimeWindDegree,
                nighttimeWindSpeed,
                nighttimeWindGusts
            ),
            nighttimeCloudCover?.toInt()
        ),
        DegreeDay(degreeDayHeating, degreeDayCooling),
        Astro(sunRiseDate?.let { Date(it) }, sunSetDate?.let { Date(it) }),
        Astro(moonRiseDate?.let { Date(it) }, moonSetDate?.let { Date(it) }),
        MoonPhase(moonPhaseAngle?.toInt()),
        AirQuality(
            pm25,
            pm10,
            so2,
            no2,
            o3,
            co
        ),
        Pollen(
            alder = alder?.toInt(),
            ash = ash?.toInt(),
            birch = birch?.toInt(),
            chestnut = chestnut?.toInt(),
            cypress = cypress?.toInt(),
            grass = grass?.toInt(),
            hazel = hazel?.toInt(),
            hornbeam = hornbeam?.toInt(),
            linden = linden?.toInt(),
            mugwort = mugwort?.toInt(),
            mold = mold?.toInt(),
            oak = oak?.toInt(),
            olive = olive?.toInt(),
            plane = plane?.toInt(),
            plantain = plantain?.toInt(),
            poplar = poplar?.toInt(),
            ragweed = ragweed?.toInt(),
            sorrel = sorrel?.toInt(),
            tree = tree?.toInt(),
            urticaceae = urticaceae?.toInt(),
            willow = willow?.toInt()
        ),
        UV(uvIndex),
        sunshineDuration
    )

    fun mapHourly(
        date: Long,
        daylight: Boolean,
        weatherText: String?,
        weatherCode: WeatherCode?,
        temperature: Double?,
        realFeelTemperature: Double?,
        realFeelShaderTemperature: Double?,
        apparentTemperature: Double?,
        windChillTemperature: Double?,
        wetBulbTemperature: Double?,
        totalPrecipitation: Double?,
        thunderstormPrecipitation: Double?,
        rainPrecipitation: Double?,
        snowPrecipitation: Double?,
        icePrecipitation: Double?,
        totalPrecipitationProbability: Double?,
        thunderstormPrecipitationProbability: Double?,
        rainPrecipitationProbability: Double?,
        snowPrecipitationProbability: Double?,
        icePrecipitationProbability: Double?,
        windDegree: Double?,
        windSpeed: Double?,
        windGusts: Double?,
        pm25: Double?,
        pm10: Double?,
        so2: Double?,
        no2: Double?,
        o3: Double?,
        co: Double?,
        uvIndex: Double?,
        relativeHumidity: Double?,
        dewPoint: Double?,
        pressure: Double?,
        cloudCover: Long?,
        visibility: Double?,
    ): Hourly = Hourly(
        Date(date),
        daylight,
        weatherText,
        weatherCode,
        Temperature(
            temperature,
            realFeelTemperature,
            realFeelShaderTemperature,
            apparentTemperature,
            windChillTemperature,
            wetBulbTemperature
        ),
        Precipitation(
            totalPrecipitation,
            thunderstormPrecipitation,
            rainPrecipitation,
            snowPrecipitation,
            icePrecipitation
        ),
        PrecipitationProbability(
            totalPrecipitationProbability,
            thunderstormPrecipitationProbability,
            rainPrecipitationProbability,
            snowPrecipitationProbability,
            icePrecipitationProbability
        ),
        Wind(
            windDegree,
            windSpeed,
            windGusts
        ),
        AirQuality(
            pm25,
            pm10,
            so2,
            no2,
            o3,
            co
        ),
        UV(uvIndex),
        relativeHumidity,
        dewPoint,
        pressure,
        cloudCover?.toInt(),
        visibility
    )

    fun mapAlert(
        alertId: String,
        startDate: Long?,
        endDate: Long?,
        headline: String?,
        description: String?,
        instruction: String?,
        source: String?,
        severity: AlertSeverity,
        color: Long,
    ): Alert = Alert(
        alertId = alertId,
        startDate = startDate?.let { Date(it) },
        endDate = endDate?.let { Date(it) },
        headline = headline,
        description = description,
        instruction = instruction,
        source = source,
        severity = severity,
        color = color.toInt()
    )

    fun mapMinutely(
        date: Long,
        minuteInterval: Long,
        intensity: Double?,
    ): Minutely = Minutely(
        Date(date),
        minuteInterval.toInt(),
        intensity
    )
}

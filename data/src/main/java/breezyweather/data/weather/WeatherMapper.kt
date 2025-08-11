/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package breezyweather.data.weather

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Base
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyDewPoint
import breezyweather.domain.weather.model.DailyPressure
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.DailyVisibility
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
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.unit.pressure.Pressure
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
        normalsUpdateLatitude: Double,
        normalsUpdateLongitude: Double,
        weatherText: String?,
        weatherCode: WeatherCode?,
        temperature: Double?,
        sourceFeelsLikeTemperature: Double?,
        apparentTemperature: Double?,
        windChillTemperature: Double?,
        humidex: Double?,
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
        pressure: Pressure?,
        visibility: Double?,
        cloudCover: Long?,
        ceiling: Double?,
        dailyForecast: String?,
        hourlyForecast: String?,
    ): Weather = Weather(
        Base(
            refreshTime?.let { Date(it) },
            mainUpdateTime?.let { Date(it) },
            currentUpdateTime?.let { Date(it) },
            airQualityUpdateTime?.let { Date(it) },
            pollenUpdateTime?.let { Date(it) },
            minutelyUpdateTime?.let { Date(it) },
            alertsUpdateTime?.let { Date(it) },
            normalsUpdateTime?.let { Date(it) },
            normalsUpdateLatitude,
            normalsUpdateLongitude
        ),
        Current(
            weatherText,
            weatherCode,
            Temperature(
                temperature,
                sourceFeelsLike = sourceFeelsLikeTemperature,
                computedApparent = apparentTemperature,
                computedWindChill = windChillTemperature,
                computedHumidex = humidex
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
        )
    )

    fun mapDaily(
        date: Long,
        daytimeWeatherText: String?,
        daytimeweatherSummary: String?,
        daytimeWeatherCode: WeatherCode?,
        daytimeTemperature: Double?,
        daytimeSourceFeelsLikeTemperature: Double?,
        daytimeApparentTemperature: Double?,
        daytimeWindChillTemperature: Double?,
        daytimeHumidex: Double?,
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
        nighttimeWeatherText: String?,
        nighttimeweatherSummary: String?,
        nighttimeWeatherCode: WeatherCode?,
        nighttimeTemperature: Double?,
        nighttimeSourceFeelsLikeTemperature: Double?,
        nighttimeApparentTemperature: Double?,
        nighttimeWindChillTemperature: Double?,
        nighttimeHumidex: Double?,
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
        degreeDayHeating: Double?,
        degreeDayCooling: Double?,
        sunRiseDate: Long?,
        sunSetDate: Long?,
        twilightRiseDate: Long?,
        twilightSetDate: Long?,
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
        relativeHumidityAverage: Double?,
        relativeHumidityMin: Double?,
        relativeHumidityMax: Double?,
        dewpointAverage: Double?,
        dewpointMin: Double?,
        dewpointMax: Double?,
        pressureAverage: Pressure?,
        pressureMin: Pressure?,
        pressureMax: Pressure?,
        cloudCoverAverage: Long?,
        cloudCoverMin: Long?,
        cloudCoverMax: Long?,
        visibilityAverage: Double?,
        visibilityMin: Double?,
        visibilityMax: Double?,
    ): Daily = Daily(
        Date(date),
        HalfDay(
            daytimeWeatherText,
            daytimeweatherSummary,
            daytimeWeatherCode,
            Temperature(
                daytimeTemperature,
                sourceFeelsLike = daytimeSourceFeelsLikeTemperature,
                daytimeApparentTemperature,
                daytimeWindChillTemperature,
                daytimeHumidex
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
            )
        ),
        HalfDay(
            nighttimeWeatherText,
            nighttimeweatherSummary,
            nighttimeWeatherCode,
            Temperature(
                nighttimeTemperature,
                sourceFeelsLike = nighttimeSourceFeelsLikeTemperature,
                nighttimeApparentTemperature,
                nighttimeWindChillTemperature,
                nighttimeHumidex
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
            )
        ),
        DegreeDay(degreeDayHeating, degreeDayCooling),
        Astro(sunRiseDate?.let { Date(it) }, sunSetDate?.let { Date(it) }),
        Astro(twilightRiseDate?.let { Date(it) }, twilightSetDate?.let { Date(it) }),
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
        sunshineDuration,
        relativeHumidity = DailyRelativeHumidity(
            average = relativeHumidityAverage,
            min = relativeHumidityMin,
            max = relativeHumidityMax
        ),
        dewPoint = DailyDewPoint(
            average = dewpointAverage,
            min = dewpointMin,
            max = dewpointMax
        ),
        pressure = DailyPressure(
            average = pressureAverage,
            min = pressureMin,
            max = pressureMax
        ),
        cloudCover = DailyCloudCover(
            average = cloudCoverAverage?.toInt(),
            min = cloudCoverMin?.toInt(),
            max = cloudCoverMax?.toInt()
        ),
        visibility = DailyVisibility(
            average = visibilityAverage,
            min = visibilityMin,
            max = visibilityMax
        )
    )

    fun mapHourly(
        date: Long,
        daylight: Boolean,
        weatherText: String?,
        weatherCode: WeatherCode?,
        temperature: Double?,
        sourceFeelsLikeTemperature: Double?,
        apparentTemperature: Double?,
        windChillTemperature: Double?,
        humidex: Double?,
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
        pressure: Pressure?,
        cloudCover: Long?,
        visibility: Double?,
    ): Hourly = Hourly(
        Date(date),
        daylight,
        weatherText,
        weatherCode,
        Temperature(
            temperature,
            sourceFeelsLike = sourceFeelsLikeTemperature,
            computedApparent = apparentTemperature,
            computedWindChill = windChillTemperature,
            computedHumidex = humidex
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

    fun mapNormals(
        month: Long,
        daytimeTemperature: Double?,
        nighttimeTemperature: Double?,
    ): Map<Month, Normals> = mapOf(
        Month.of(month.toInt()) to Normals(
            daytimeTemperature,
            nighttimeTemperature
        )
    )
}

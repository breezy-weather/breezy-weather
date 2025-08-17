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
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import org.breezyweather.unit.distance.Distance
import org.breezyweather.unit.pollen.PollenConcentration
import org.breezyweather.unit.pollutant.PollutantConcentration
import org.breezyweather.unit.precipitation.Precipitation
import org.breezyweather.unit.pressure.Pressure
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.speed.Speed
import org.breezyweather.unit.temperature.Temperature
import java.util.Date
import kotlin.time.Duration

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
        temperature: Temperature?,
        sourceFeelsLikeTemperature: Temperature?,
        apparentTemperature: Temperature?,
        windChillTemperature: Temperature?,
        humidex: Temperature?,
        windDegree: Double?,
        windSpeed: Speed?,
        windGusts: Speed?,
        uvIndex: Double?,
        pm25: PollutantConcentration?,
        pm10: PollutantConcentration?,
        so2: PollutantConcentration?,
        no2: PollutantConcentration?,
        o3: PollutantConcentration?,
        co: PollutantConcentration?,
        relativeHumidity: Ratio?,
        dewPoint: Temperature?,
        pressure: Pressure?,
        visibility: Distance?,
        cloudCover: Ratio?,
        ceiling: Distance?,
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
            breezyweather.domain.weather.model.Temperature(
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
            cloudCover,
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
        daytimeTemperature: Temperature?,
        daytimeSourceFeelsLikeTemperature: Temperature?,
        daytimeApparentTemperature: Temperature?,
        daytimeWindChillTemperature: Temperature?,
        daytimeHumidex: Temperature?,
        daytimeTotalPrecipitation: Precipitation?,
        daytimeThunderstormPrecipitation: Precipitation?,
        daytimeRainPrecipitation: Precipitation?,
        daytimeSnowPrecipitation: Precipitation?,
        daytimeIcePrecipitation: Precipitation?,
        daytimeTotalPrecipitationProbability: Ratio?,
        daytimeThunderstormPrecipitationProbability: Ratio?,
        daytimeRainPrecipitationProbability: Ratio?,
        daytimeSnowPrecipitationProbability: Ratio?,
        daytimeIcePrecipitationProbability: Ratio?,
        daytimeTotalPrecipitationDuration: Duration?,
        daytimeThunderstormPrecipitationDuration: Duration?,
        daytimeRainPrecipitationDuration: Duration?,
        daytimeSnowPrecipitationDuration: Duration?,
        daytimeIcePrecipitationDuration: Duration?,
        daytimeWindDegree: Double?,
        daytimeWindSpeed: Speed?,
        daytimeWindGusts: Speed?,
        nighttimeWeatherText: String?,
        nighttimeweatherSummary: String?,
        nighttimeWeatherCode: WeatherCode?,
        nighttimeTemperature: Temperature?,
        nighttimeSourceFeelsLikeTemperature: Temperature?,
        nighttimeApparentTemperature: Temperature?,
        nighttimeWindChillTemperature: Temperature?,
        nighttimeHumidex: Temperature?,
        nighttimeTotalPrecipitation: Precipitation?,
        nighttimeThunderstormPrecipitation: Precipitation?,
        nighttimeRainPrecipitation: Precipitation?,
        nighttimeSnowPrecipitation: Precipitation?,
        nighttimeIcePrecipitation: Precipitation?,
        nighttimeTotalPrecipitationProbability: Ratio?,
        nighttimeThunderstormPrecipitationProbability: Ratio?,
        nighttimeRainPrecipitationProbability: Ratio?,
        nighttimeSnowPrecipitationProbability: Ratio?,
        nighttimeIcePrecipitationProbability: Ratio?,
        nighttimeTotalPrecipitationDuration: Duration?,
        nighttimeThunderstormPrecipitationDuration: Duration?,
        nighttimeRainPrecipitationDuration: Duration?,
        nighttimeSnowPrecipitationDuration: Duration?,
        nighttimeIcePrecipitationDuration: Duration?,
        nighttimeWindDegree: Double?,
        nighttimeWindSpeed: Speed?,
        nighttimeWindGusts: Speed?,
        degreeDayHeating: Temperature?,
        degreeDayCooling: Temperature?,
        sunRiseDate: Long?,
        sunSetDate: Long?,
        twilightRiseDate: Long?,
        twilightSetDate: Long?,
        moonRiseDate: Long?,
        moonSetDate: Long?,
        moonPhaseAngle: Long?,
        pm25: PollutantConcentration?,
        pm10: PollutantConcentration?,
        so2: PollutantConcentration?,
        no2: PollutantConcentration?,
        o3: PollutantConcentration?,
        co: PollutantConcentration?,
        alder: PollenConcentration?,
        ash: PollenConcentration?,
        birch: PollenConcentration?,
        chestnut: PollenConcentration?,
        cypress: PollenConcentration?,
        grass: PollenConcentration?,
        hazel: PollenConcentration?,
        hornbeam: PollenConcentration?,
        linden: PollenConcentration?,
        mold: PollenConcentration?,
        mugwort: PollenConcentration?,
        oak: PollenConcentration?,
        olive: PollenConcentration?,
        plane: PollenConcentration?,
        plantain: PollenConcentration?,
        poplar: PollenConcentration?,
        ragweed: PollenConcentration?,
        sorrel: PollenConcentration?,
        tree: PollenConcentration?,
        urticaceae: PollenConcentration?,
        willow: PollenConcentration?,
        uvIndex: Double?,
        sunshineDuration: Duration?,
        relativeHumidityAverage: Ratio?,
        relativeHumidityMin: Ratio?,
        relativeHumidityMax: Ratio?,
        dewpointAverage: Temperature?,
        dewpointMin: Temperature?,
        dewpointMax: Temperature?,
        pressureAverage: Pressure?,
        pressureMin: Pressure?,
        pressureMax: Pressure?,
        cloudCoverAverage: Ratio?,
        cloudCoverMin: Ratio?,
        cloudCoverMax: Ratio?,
        visibilityAverage: Distance?,
        visibilityMin: Distance?,
        visibilityMax: Distance?,
    ): Daily = Daily(
        Date(date),
        HalfDay(
            daytimeWeatherText,
            daytimeweatherSummary,
            daytimeWeatherCode,
            breezyweather.domain.weather.model.Temperature(
                daytimeTemperature,
                sourceFeelsLike = daytimeSourceFeelsLikeTemperature,
                daytimeApparentTemperature,
                daytimeWindChillTemperature,
                daytimeHumidex
            ),
            breezyweather.domain.weather.model.Precipitation(
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
            breezyweather.domain.weather.model.Temperature(
                nighttimeTemperature,
                sourceFeelsLike = nighttimeSourceFeelsLikeTemperature,
                nighttimeApparentTemperature,
                nighttimeWindChillTemperature,
                nighttimeHumidex
            ),
            breezyweather.domain.weather.model.Precipitation(
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
            alder = alder,
            ash = ash,
            birch = birch,
            chestnut = chestnut,
            cypress = cypress,
            grass = grass,
            hazel = hazel,
            hornbeam = hornbeam,
            linden = linden,
            mugwort = mugwort,
            mold = mold,
            oak = oak,
            olive = olive,
            plane = plane,
            plantain = plantain,
            poplar = poplar,
            ragweed = ragweed,
            sorrel = sorrel,
            tree = tree,
            urticaceae = urticaceae,
            willow = willow
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
            average = cloudCoverAverage,
            min = cloudCoverMin,
            max = cloudCoverMax
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
        temperature: Temperature?,
        sourceFeelsLikeTemperature: Temperature?,
        apparentTemperature: Temperature?,
        windChillTemperature: Temperature?,
        humidex: Temperature?,
        totalPrecipitation: Precipitation?,
        thunderstormPrecipitation: Precipitation?,
        rainPrecipitation: Precipitation?,
        snowPrecipitation: Precipitation?,
        icePrecipitation: Precipitation?,
        totalPrecipitationProbability: Ratio?,
        thunderstormPrecipitationProbability: Ratio?,
        rainPrecipitationProbability: Ratio?,
        snowPrecipitationProbability: Ratio?,
        icePrecipitationProbability: Ratio?,
        windDegree: Double?,
        windSpeed: Speed?,
        windGusts: Speed?,
        pm25: PollutantConcentration?,
        pm10: PollutantConcentration?,
        so2: PollutantConcentration?,
        no2: PollutantConcentration?,
        o3: PollutantConcentration?,
        co: PollutantConcentration?,
        uvIndex: Double?,
        relativeHumidity: Ratio?,
        dewPoint: Temperature?,
        pressure: Pressure?,
        cloudCover: Ratio?,
        visibility: Distance?,
    ): Hourly = Hourly(
        Date(date),
        daylight,
        weatherText,
        weatherCode,
        breezyweather.domain.weather.model.Temperature(
            temperature,
            sourceFeelsLike = sourceFeelsLikeTemperature,
            computedApparent = apparentTemperature,
            computedWindChill = windChillTemperature,
            computedHumidex = humidex
        ),
        breezyweather.domain.weather.model.Precipitation(
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
        cloudCover,
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
        intensity: Precipitation?,
    ): Minutely = Minutely(
        Date(date),
        minuteInterval.toInt(),
        intensity
    )

    fun mapNormals(
        month: Long,
        daytimeTemperature: Temperature?,
        nighttimeTemperature: Temperature?,
    ): Map<Month, Normals> = mapOf(
        Month.of(month.toInt()) to Normals(
            daytimeTemperature,
            nighttimeTemperature
        )
    )
}

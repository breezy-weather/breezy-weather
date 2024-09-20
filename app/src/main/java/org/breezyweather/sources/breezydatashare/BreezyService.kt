/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.breezydatashare

import android.content.Context
import android.os.Bundle
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.Wind
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getConcentration
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getIndexName
import org.breezyweather.domain.weather.model.getMinutelyDescription
import org.breezyweather.domain.weather.model.getMinutelyTitle
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.validPollens
import org.breezyweather.domain.weather.model.validPollutants
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.breezydatashare.json.BreezyAirQuality
import org.breezyweather.sources.breezydatashare.json.BreezyAlert
import org.breezyweather.sources.breezydatashare.json.BreezyAstro
import org.breezyweather.sources.breezydatashare.json.BreezyBulletin
import org.breezyweather.sources.breezydatashare.json.BreezyCurrent
import org.breezyweather.sources.breezydatashare.json.BreezyDaily
import org.breezyweather.sources.breezydatashare.json.BreezyData
import org.breezyweather.sources.breezydatashare.json.BreezyDegreeDay
import org.breezyweather.sources.breezydatashare.json.BreezyDoubleUnit
import org.breezyweather.sources.breezydatashare.json.BreezyHalfDay
import org.breezyweather.sources.breezydatashare.json.BreezyHourly
import org.breezyweather.sources.breezydatashare.json.BreezyLocation
import org.breezyweather.sources.breezydatashare.json.BreezyMinutely
import org.breezyweather.sources.breezydatashare.json.BreezyMoonPhase
import org.breezyweather.sources.breezydatashare.json.BreezyNormals
import org.breezyweather.sources.breezydatashare.json.BreezyPercent
import org.breezyweather.sources.breezydatashare.json.BreezyPollen
import org.breezyweather.sources.breezydatashare.json.BreezyPollutant
import org.breezyweather.sources.breezydatashare.json.BreezyPrecipitation
import org.breezyweather.sources.breezydatashare.json.BreezyPrecipitationDuration
import org.breezyweather.sources.breezydatashare.json.BreezyPrecipitationProbability
import org.breezyweather.sources.breezydatashare.json.BreezyTemperature
import org.breezyweather.sources.breezydatashare.json.BreezyUV
import org.breezyweather.sources.breezydatashare.json.BreezyWeather
import org.breezyweather.sources.breezydatashare.json.BreezyWind
import java.text.NumberFormat
import javax.inject.Inject

class BreezyService @Inject constructor() : BroadcastSource {

    override val id = "breezydatashare"
    override val name = "Breezy Weather"

    override val intentAction = "org.breezyweather.ACTION_GENERIC_WEATHER"

    override fun getExtras(context: Context, locations: List<Location>): Bundle {
        return Bundle().apply {
            putString(
                "WeatherJson",
                Json.encodeToString(
                    BreezyData(
                        appVersion = BuildConfig.VERSION_NAME,
                        locations = locations.mapNotNull {
                            if (it.weather?.current != null) {
                                getWeatherData(context, it)
                            } else null
                        }
                    )
                )
            )
        }
    }

    private fun getWeatherData(
        context: Context,
        location: Location
    ): BreezyLocation {
        val temperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit
        val distanceUnit = SettingsManager.getInstance(context).distanceUnit
        val pressureUnit = SettingsManager.getInstance(context).pressureUnit
        val percentUnit = NumberFormat.getPercentInstance(context.currentLocale)

        // TODO: Need to create a lib for these classes so they can be included in other projects
        return BreezyLocation(
            id = "TODO", // TODO: Can't use cityId because it leaks longitude and latitude
            timezone = location.timeZone,
            country = location.country,
            countryCode = location.countryCode,
            admin1 = location.admin1,
            admin1Code = location.admin1Code,
            admin2 = location.admin2,
            admin2Code = location.admin2Code,
            admin3 = location.admin3,
            admin3Code = location.admin3Code,
            admin4 = location.admin4,
            admin4Code = location.admin4Code,
            city = location.city,
            district = location.district,
            weather = location.weather?.let { weather ->
                BreezyWeather(
                    refreshTime = weather.base.refreshTime?.time,
                    bulletin = BreezyBulletin(
                        dailyForecast = weather.current?.dailyForecast,
                        hourlyForecast = weather.current?.hourlyForecast,
                        minutelyForecastTitle = weather.getMinutelyTitle(context),
                        minutelyForecastDescription = weather.getMinutelyDescription(context, location)
                    ),
                    current = getCurrent(
                        context, weather.current,
                        temperatureUnit, distanceUnit, pressureUnit, percentUnit
                    ),
                    daily = getDaily(
                        context, weather.dailyForecast,
                        temperatureUnit, precipitationUnit, distanceUnit, percentUnit
                    ),
                    hourly = getHourly(
                        context, weather.hourlyForecast,
                        temperatureUnit, precipitationUnit, distanceUnit, pressureUnit, percentUnit
                    ),
                    minutely = getMinutely(
                        context, weather.minutelyForecast, precipitationUnit
                    ),
                    alerts = getAlerts(
                        weather.alertList
                    ),
                    normals = getNormals(
                        context, weather.normals, temperatureUnit
                    )
                )
            },
            // TODO: sources/credits
        )
    }

    private fun getNormals(context: Context, normals: Normals?, temperatureUnit: TemperatureUnit): BreezyNormals? {
        return normals?.let {
            BreezyNormals(
                month = it.month,
                daytimeTemperature = getTemperatureDoubleUnit(context, it.daytimeTemperature, temperatureUnit),
                nighttimeTemperature = getTemperatureDoubleUnit(context, it.nighttimeTemperature, temperatureUnit)
            )
        }
    }

    private fun getCurrent(
        context: Context,
        current: Current?,
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        percentUnit: NumberFormat
    ): BreezyCurrent? {
        return current?.let { cur ->
            BreezyCurrent(
                weatherText = cur.weatherText,
                weatherCode = cur.weatherCode?.id,
                temperature = getTemperature(context, cur.temperature, temperatureUnit),
                wind = getWind(context, cur.wind, distanceUnit),
                uV = cur.uV?.let { BreezyUV(it.index) },
                airQuality = getAirQuality(context, cur.airQuality),
                relativeHumidity = getPercentUnit(cur.relativeHumidity, percentUnit, 0),
                dewPoint = getTemperatureDoubleUnit(context, cur.dewPoint, temperatureUnit),
                pressure = getPressureDoubleUnit(context, cur.pressure, pressureUnit),
                cloudCover = getPercentUnit(cur.cloudCover?.toDouble(), percentUnit, 0),
                visibility = getDistanceDoubleUnit(context, cur.visibility, distanceUnit),
                ceiling = getDistanceDoubleUnit(context, cur.ceiling, distanceUnit)
            )
        }
    }

    private fun getDaily(
        context: Context,
        daily: List<Daily>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        percentUnit: NumberFormat
    ): List<BreezyDaily>? {
        return daily?.map { day ->
            BreezyDaily(
                date = day.date.time,
                day = getHalfDay(
                    context, day.day, temperatureUnit, precipitationUnit, distanceUnit, percentUnit
                ),
                night = getHalfDay(
                    context, day.night, temperatureUnit, precipitationUnit, distanceUnit, percentUnit
                ),
                degreeDay = day.degreeDay?.let {
                    BreezyDegreeDay(
                        heating = getDegreeDayTemperatureDoubleUnit(context, it.heating, temperatureUnit),
                        cooling = getDegreeDayTemperatureDoubleUnit(context, it.cooling, temperatureUnit)
                    )
                },
                sun = getAstro(day.sun),
                moon = getAstro(day.moon),
                moonPhase = day.moonPhase?.let {
                    BreezyMoonPhase(
                        angle = it.angle,
                        description = it.getDescription(context)
                    )
                },
                airQuality = getAirQuality(context, day.airQuality),
                pollen = getPollen(context, day.pollen),
                uV = day.uV?.let { BreezyUV(it.index) },
                sunshineDuration = getDurationUnit(context, day.sunshineDuration)
            )
        }
    }

    private fun getHourly(
        context: Context,
        hourly: List<Hourly>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        percentUnit: NumberFormat
    ): List<BreezyHourly>? {
        return hourly?.map { hour ->
            BreezyHourly(
                date = hour.date.time,
                isDaylight = hour.isDaylight,
                weatherText = hour.weatherText,
                weatherCode = hour.weatherCode?.id,
                temperature = getTemperature(context, hour.temperature, temperatureUnit),
                precipitation = getPrecipitation(context, hour.precipitation, precipitationUnit),
                precipitationProbability = getPrecipitationProbability(hour.precipitationProbability, percentUnit),
                wind = getWind(context, hour.wind, distanceUnit),
                airQuality = getAirQuality(context, hour.airQuality),
                uV = hour.uV?.let { BreezyUV(it.index) },
                relativeHumidity = getPercentUnit(hour.relativeHumidity, percentUnit, 0),
                dewPoint = getTemperatureDoubleUnit(context, hour.dewPoint, temperatureUnit),
                pressure = getPressureDoubleUnit(context, hour.pressure, pressureUnit),
                cloudCover = getPercentUnit(hour.cloudCover?.toDouble(), percentUnit, 0),
                visibility = getDistanceDoubleUnit(context, hour.visibility, distanceUnit),
            )
        }
    }

    private fun getMinutely(
        context: Context,
        minutely: List<Minutely>?,
        precipitationUnit: PrecipitationUnit
    ): List<BreezyMinutely>? {
        return minutely?.map { minute ->
            BreezyMinutely(
                date = minute.date.time,
                minuteInterval = minute.minuteInterval,
                precipitationIntensity = getPrecipitationDoubleUnit(
                    context, minute.precipitationIntensity, precipitationUnit
                )
            )
        }
    }

    private fun getAstro(astro: Astro?): BreezyAstro? {
        return astro?.let {
            BreezyAstro(
                riseDate = it.riseDate?.time,
                setDate = it.setDate?.time
            )
        }
    }

    private fun getHalfDay(
        context: Context,
        halfDay: HalfDay?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        distanceUnit: DistanceUnit,
        percentUnit: NumberFormat
    ): BreezyHalfDay? {
        return halfDay?.let { hd ->
            BreezyHalfDay(
                weatherText = hd.weatherText,
                weatherPhase = hd.weatherPhase,
                weatherCode = hd.weatherCode?.id,
                temperature = getTemperature(context, hd.temperature, temperatureUnit),
                precipitation = getPrecipitation(context, hd.precipitation, precipitationUnit),
                precipitationProbability = getPrecipitationProbability(
                    hd.precipitationProbability, percentUnit
                ),
                precipitationDuration = getPrecipitationDuration(context, hd.precipitationDuration),
                wind = getWind(context, hd.wind, distanceUnit),
                cloudCover = getPercentUnit(hd.cloudCover?.toDouble(), percentUnit, 0)
            )
        }
    }

    private fun getTemperature(
        context: Context,
        temperature: Temperature?,
        temperatureUnit: TemperatureUnit
    ): BreezyTemperature? {
        return temperature?.let {
            BreezyTemperature(
                temperature = getTemperatureDoubleUnit(context, it.temperature, temperatureUnit),
                realFeelTemperature = getTemperatureDoubleUnit(context, it.realFeelTemperature, temperatureUnit),
                realFeelShaderTemperature = getTemperatureDoubleUnit(context, it.realFeelShaderTemperature, temperatureUnit),
                apparentTemperature = getTemperatureDoubleUnit(context, it.apparentTemperature, temperatureUnit),
                windChillTemperature = getTemperatureDoubleUnit(context, it.windChillTemperature, temperatureUnit),
                wetBulbTemperature = getTemperatureDoubleUnit(context, it.wetBulbTemperature, temperatureUnit),
            )
        }
    }

    private fun getTemperatureDoubleUnit(
        context: Context,
        temperature: Double?,
        temperatureUnit: TemperatureUnit
    ): BreezyDoubleUnit? {
        return temperature?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "c",
                preferredUnitValue = temperatureUnit.convertUnit(it),
                preferredUnitUnit = temperatureUnit.id,
                preferredUnitFormatted = temperatureUnit.getValueText(context, it, 0),
                preferredUnitFormattedShort = temperatureUnit.getShortValueText(context, it)
            )
        }
    }

    /**
     * FIXME: Not consistant with getTemperatureDoubleUnit on formatted
     * (missing equivalent ValueText functions)
     */
    private fun getDegreeDayTemperatureDoubleUnit(
        context: Context,
        temperature: Double?,
        temperatureUnit: TemperatureUnit
    ): BreezyDoubleUnit? {
        return temperature?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "c",
                preferredUnitValue = temperatureUnit.convertDegreeDayUnit(it),
                preferredUnitUnit = temperatureUnit.id,
                preferredUnitFormatted = temperatureUnit.getDegreeDayValueText(context, it),
                preferredUnitFormattedShort = temperatureUnit.getDegreeDayValueText(context, it)
            )
        }
    }

    private fun getPrecipitation(
        context: Context,
        precipitation: Precipitation?,
        precipitationUnit: PrecipitationUnit
    ): BreezyPrecipitation? {
        return precipitation?.let {
            BreezyPrecipitation(
                total = getPrecipitationDoubleUnit(context, it.total, precipitationUnit),
                thunderstorm = getPrecipitationDoubleUnit(context, it.thunderstorm, precipitationUnit),
                rain = getPrecipitationDoubleUnit(context, it.rain, precipitationUnit),
                snow = getPrecipitationDoubleUnit(context, it.snow, precipitationUnit),
                ice = getPrecipitationDoubleUnit(context, it.ice, precipitationUnit)
            )
        }
    }

    private fun getPrecipitationDoubleUnit(
        context: Context,
        precipitation: Double?,
        precipitationUnit: PrecipitationUnit
    ): BreezyDoubleUnit? {
        return precipitation?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "mm",
                preferredUnitValue = precipitationUnit.convertUnit(it),
                preferredUnitUnit = precipitationUnit.id,
                preferredUnitFormatted = precipitationUnit.getValueText(context, it),
                preferredUnitFormattedShort = precipitationUnit.getValueText(context, it)
            )
        }
    }

    private fun getPrecipitationProbability(
        precipitationProbability: PrecipitationProbability?,
        percentUnit: NumberFormat
    ): BreezyPrecipitationProbability? {
        return precipitationProbability?.let {
            BreezyPrecipitationProbability(
                total = getPercentUnit(it.total, percentUnit),
                thunderstorm = getPercentUnit(it.thunderstorm, percentUnit),
                rain = getPercentUnit(it.rain, percentUnit),
                snow = getPercentUnit(it.snow, percentUnit),
                ice = getPercentUnit(it.ice, percentUnit)
            )
        }
    }

    private fun getPrecipitationDuration(
        context: Context,
        precipitationDuration: PrecipitationDuration?
    ): BreezyPrecipitationDuration? {
        return precipitationDuration?.let {
            BreezyPrecipitationDuration(
                total = getDurationUnit(context, it.total),
                thunderstorm = getDurationUnit(context, it.thunderstorm),
                rain = getDurationUnit(context, it.rain),
                snow = getDurationUnit(context, it.snow),
                ice = getDurationUnit(context, it.ice)
            )
        }
    }

    private fun getWind(
        context: Context, wind: Wind?, distanceUnit: DistanceUnit
    ): BreezyWind? {
        return wind?.let {
            BreezyWind(
                degree = wind.degree,
                speed = getDistanceDoubleUnit(context, wind.speed, distanceUnit),
                gusts = getDistanceDoubleUnit(context, wind.gusts, distanceUnit)
            )
        }
    }

    private fun getDistanceDoubleUnit(
        context: Context,
        distance: Double?,
        distanceUnit: DistanceUnit
    ): BreezyDoubleUnit? {
        return distance?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "m",
                preferredUnitValue = distanceUnit.convertUnit(it),
                preferredUnitUnit = distanceUnit.id,
                preferredUnitFormatted = distanceUnit.getValueText(context, it),
                preferredUnitFormattedShort = distanceUnit.getValueText(context, it)
            )
        }
    }

    private fun getPressureDoubleUnit(
        context: Context,
        pressure: Double?,
        pressureUnit: PressureUnit
    ): BreezyDoubleUnit? {
        return pressure?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "mb",
                preferredUnitValue = pressureUnit.convertUnit(it),
                preferredUnitUnit = pressureUnit.id,
                preferredUnitFormatted = pressureUnit.getValueText(context, it),
                preferredUnitFormattedShort = pressureUnit.getValueText(context, it)
            )
        }
    }

    private fun getPercentUnit(
        percent: Double?,
        percentUnit: NumberFormat,
        digits: Int = 0
    ): BreezyPercent? {
        return percent?.let {
            BreezyPercent(
                value = it,
                formatted = percentUnit.apply {
                    maximumFractionDigits = digits
                }.format(it.div(100.0))
            )
        }
    }

    private fun getDurationUnit(
        context: Context, duration: Double?
    ): BreezyDoubleUnit? {
        return duration?.let {
            BreezyDoubleUnit(
                originalValue = it,
                originalUnit = "h",
                preferredUnitValue = it,
                preferredUnitUnit = "h",
                preferredUnitFormatted = DurationUnit.H.getValueText(context, it),
                preferredUnitFormattedShort = DurationUnit.H.getValueText(context, it)
            )
        }
    }

    private fun getAirQuality(
        context: Context, airQuality: AirQuality?
    ): BreezyAirQuality? {
        return airQuality?.let {
            if (airQuality.isValid) {
                BreezyAirQuality(
                    index = airQuality.getIndex(),
                    indexColor = airQuality.getColor(context),
                    pollutants = airQuality.validPollutants.associate {
                        it.id to BreezyPollutant(
                            id = it.id,
                            name = airQuality.getName(context, it),
                            concentration = airQuality.getConcentration(it),
                            index = airQuality.getIndex(it),
                            color = airQuality.getColor(context, it)
                        )
                    }
                )
            } else null
        }
    }

    /**
     * TODO: Support for source-based pollen index
     */
    private fun getPollen(
        context: Context, pollen: Pollen?
    ): Map<String, BreezyPollen>? {
        return pollen?.let {
            if (it.isValid) {
                it.validPollens.associate { component ->
                    component.id to BreezyPollen(
                        id = component.id,
                        name = it.getName(context, component),
                        concentration = it.getConcentration(component), // TODO: Remove when from source
                        indexName = it.getIndexName(context, component), // TODO: getIndexNameFromSource when applies
                        color = it.getColor(context, component)
                    )
                }
            } else null
        }
    }

    private fun getAlerts(alertList: List<Alert>?): List<BreezyAlert>? {
        return alertList?.map { alert ->
            BreezyAlert(
                alertId = alert.alertId,
                startDate = alert.startDate?.time,
                endDate = alert.endDate?.time,
                headline = alert.headline,
                description = alert.description,
                instruction = alert.instruction,
                source = alert.source,
                severity = alert.severity.id,
                color = alert.color
            )
        }
    }
}

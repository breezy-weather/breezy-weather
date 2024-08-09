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
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.Wind
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.breezydatashare.json.BreezyAirQuality
import org.breezyweather.sources.breezydatashare.json.BreezyCurrent
import org.breezyweather.sources.breezydatashare.json.BreezyDaily
import org.breezyweather.sources.breezydatashare.json.BreezyData
import org.breezyweather.sources.breezydatashare.json.BreezyDoubleUnit
import org.breezyweather.sources.breezydatashare.json.BreezyLocation
import org.breezyweather.sources.breezydatashare.json.BreezyPercent
import org.breezyweather.sources.breezydatashare.json.BreezyPollutant
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
        val distanceUnit = SettingsManager.getInstance(context).distanceUnit
        val pressureUnit = SettingsManager.getInstance(context).pressureUnit
        val percentUnit = NumberFormat.getPercentInstance(context.currentLocale)

        // TODO: Need to create a lib for these classes so they can be included in other projects
        return BreezyLocation(
            id = "TODO", // TODO: Can't use cityId because it leaks longitude and latitude
            timezone = location.timeZone,
            country = location.country,
            countryCode = location.countryCode,
            province = location.province,
            provinceCode = location.provinceCode,
            city = location.city,
            district = location.district,
            weather = location.weather?.let { weather ->
                BreezyWeather(
                    refreshTime = weather.base.refreshTime?.time,
                    current = getCurrent(
                        context, weather.current, temperatureUnit, distanceUnit, pressureUnit, percentUnit
                    )
                )
            }
        )
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
                relativeHumidity = cur.relativeHumidity?.let {
                    BreezyPercent(
                        value = it,
                        formatted = percentUnit.apply {
                            maximumFractionDigits = 0
                        }.format(it.div(100.0))
                    )
                },
                dewPoint = getTemperatureDoubleUnit(context, cur.dewPoint, temperatureUnit),
                pressure = cur.pressure?.let {
                    BreezyDoubleUnit(
                        originalValue = it,
                        originalUnit = "mb",
                        preferredUnitValue = pressureUnit.convertUnit(it),
                        preferredUnitUnit = pressureUnit.id,
                        preferredUnitFormatted = pressureUnit.getValueText(context, it),
                        preferredUnitFormattedShort = pressureUnit.getValueText(context, it)
                    )
                },
                cloudCover = cur.cloudCover?.let {
                    BreezyPercent(
                        value = it.toDouble(),
                        formatted = percentUnit.apply {
                            maximumFractionDigits = 0
                        }.format(it.div(100.0))
                    )
                },
                visibility = getDistanceDoubleUnit(context, cur.visibility, distanceUnit),
                ceiling = getDistanceDoubleUnit(context, cur.ceiling, distanceUnit),
                dailyForecast = cur.dailyForecast,
                hourlyForecast = cur.hourlyForecast
            )
        }
    }

    private fun getDaily(
        context: Context,
        daily: List<Daily>?,
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        percentUnit: NumberFormat
    ): List<BreezyDaily>? {
        return daily?.map { day ->
            BreezyDaily(
                date = day.date.time,
                //day = null,
                //night = null,
                //degreeDay = null,
                //sun = null,
                //moon = null,
                //moonPhase = null,
                airQuality = getAirQuality(context, day.airQuality),
                //pollen: BreezyPollen? = null,
                uV = day.uV?.let { BreezyUV(it.index) },
                sunshineDuration = day.sunshineDuration?.let {
                    BreezyDoubleUnit(
                        originalValue = it,
                        originalUnit = "h",
                        preferredUnitValue = it,
                        preferredUnitUnit = "h",
                        preferredUnitFormatted = DurationUnit.H.getValueText(context, it),
                        preferredUnitFormattedShort = DurationUnit.H.getValueText(context, it)
                    )
                }
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

    private fun getWind(context: Context, wind: Wind?, distanceUnit: DistanceUnit): BreezyWind? {
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

    private fun getAirQuality(context: Context, airQuality: AirQuality?): BreezyAirQuality? {
        return airQuality?.let {
            BreezyAirQuality(
                index = airQuality.getIndex(),
                indexColor = airQuality.getColor(context),
                pollutants = listOfNotNull(
                    airQuality.pM25?.let {
                        BreezyPollutant(
                            id = "pm25",
                            name = airQuality.getName(context, PollutantIndex.PM25),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.PM25),
                            color = airQuality.getColor(context, PollutantIndex.PM25)
                        )
                    },
                    airQuality.pM10?.let {
                        BreezyPollutant(
                            id = "pm10",
                            name = airQuality.getName(context, PollutantIndex.PM10),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.PM10),
                            color = airQuality.getColor(context, PollutantIndex.PM10)
                        )
                    },
                    airQuality.sO2?.let {
                        BreezyPollutant(
                            id = "so2",
                            name = airQuality.getName(context, PollutantIndex.SO2),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.SO2),
                            color = airQuality.getColor(context, PollutantIndex.SO2)
                        )
                    },
                    airQuality.nO2?.let {
                        BreezyPollutant(
                            id = "no2",
                            name = airQuality.getName(context, PollutantIndex.NO2),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.NO2),
                            color = airQuality.getColor(context, PollutantIndex.NO2)
                        )
                    },
                    airQuality.o3?.let {
                        BreezyPollutant(
                            id = "o3",
                            name = airQuality.getName(context, PollutantIndex.O3),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.O3),
                            color = airQuality.getColor(context, PollutantIndex.O3)
                        )
                    },
                    airQuality.cO?.let {
                        BreezyPollutant(
                            id = "co",
                            name = airQuality.getName(context, PollutantIndex.CO),
                            concentration = it,
                            index = airQuality.getIndex(PollutantIndex.CO),
                            color = airQuality.getColor(context, PollutantIndex.CO)
                        )
                    },
                )
            )
        }
    }
}

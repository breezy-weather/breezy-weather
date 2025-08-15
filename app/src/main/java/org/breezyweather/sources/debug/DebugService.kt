/**
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

package org.breezyweather.sources.debug

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class DebugService @Inject constructor() : WeatherSource {

    override val id = "debug"
    override val name = "Debug"

    private val weatherAttribution = "Debug"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return rxObservable {
            send(
                WeatherWrapper(
                    dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                        getDailyList(location)
                    } else {
                        null
                    },
                    hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                        getHourlyList(location)
                    } else {
                        null
                    },
                    minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                        getMinutelyList(location)
                    } else {
                        null
                    },
                    current = if (SourceFeature.CURRENT in requestedFeatures) {
                        getCurrent(location)
                    } else {
                        null
                    }
                )
            )
        }
    }

    /**
     * Always empty
     *
     * TODO: Add data for testing
     */
    private fun getDailyList(location: Location): List<DailyWrapper> {
        val calendar = Date().toCalendarWithTimeZone(location.timeZone).apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return buildList {
            add(DailyWrapper(calendar.time))
            for (i in 1..5) {
                calendar.apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                add(DailyWrapper(calendar.time))
            }
        }
    }

    /**
     * Always empty
     *
     * TODO: Add data for testing
     * TODO: Add data during DST change period
     */
    private fun getHourlyList(location: Location): List<HourlyWrapper> {
        return emptyList()
    }

    private fun getCurrent(location: Location): CurrentWrapper {
        return CurrentWrapper(
            weatherCode = WeatherCode.entries
                .firstOrNull { "$CURRENT_CITY_LABEL${it.name}" == location.city }
                ?: WeatherCode.CLEAR,
            temperature = TemperatureWrapper(
                temperature = Math.random().times(10).plus(15).celsius
            ),
            wind = Wind(
                degree = Math.random().times(360), // TODO: Add loop case: -1
                speed = Math.random().times(10).plus(10).metersPerSecond
            ),
            uV = UV(index = Math.random().times(12)),
            dewPoint = Math.random().times(10).plus(5).celsius,
            pressure = Math.random().times(100).plus(963).hectopascals,
            visibility = Math.random().times(50000).meters,
            cloudCover = Math.random().times(100).roundToInt()
        )
    }

    private fun getMinutelyList(location: Location): List<Minutely> {
        // Needed to get rounded minutes
        val startingDate = Date().time.milliseconds.inWholeMinutes.minutes.inWholeMilliseconds.toDate()
        return arrayOf(
            MINUTELY_NO_PRECIPITATION,
            MINUTELY_ONE_PRECIPITATION,
            MINUTELY_TWO_PRECIPITATION,
            MINUTELY_THREE_PRECIPITATION,
            MINUTELY_INTERVAL_1,
            MINUTELY_INTERVAL_5,
            MINUTELY_INTERVAL_15
        ).firstOrNull { "$MINUTELY_CITY_LABEL$it" == location.city }.let {
            when (it) {
                MINUTELY_NO_PRECIPITATION -> listOf(Minutely(startingDate, 5, null))
                MINUTELY_ONE_PRECIPITATION -> generateMinutelyList(startingDate, 1)
                MINUTELY_TWO_PRECIPITATION -> generateMinutelyList(startingDate, 2)
                MINUTELY_THREE_PRECIPITATION -> generateMinutelyList(startingDate, 3)
                MINUTELY_INTERVAL_1 -> generateMinutelyList(startingDate, 90, interval = 1)
                MINUTELY_INTERVAL_5 -> generateMinutelyList(startingDate, 18, interval = 5)
                else -> generateMinutelyList(startingDate, 8)
            }
        }
    }

    private fun generateMinutelyList(startingDate: Date, times: Int, interval: Int = 15): List<Minutely> {
        return buildList {
            add(Minutely(startingDate, interval, Random.nextDouble().times(20).millimeters))
            if (times > 1) {
                for (i in 1..<times) {
                    val date = Date(startingDate.time + (i * interval).minutes.inWholeMilliseconds)
                    add(
                        Minutely(
                            date,
                            interval,
                            Random.nextDouble().times(20).let {
                                if (i < 3) {
                                    it
                                } else {
                                    if (it > 10) null else it
                                }
                            }?.millimeters
                        )
                    )
                }
            }
        }
    }

    // TODO: Add more cases
    override val testingLocations: List<Location> = buildList {
        WeatherCode.entries.forEachIndexed { index, weatherCode ->
            add(
                Location(
                    city = "$CURRENT_CITY_LABEL${weatherCode.name}",
                    latitude = CURRENT_LATITUDE,
                    longitude = CURRENT_LONGITUDE_START + index * 0.01, // 2.00, 2.01, 2.02, 2.03, etc
                    timeZone = TimeZone.getTimeZone("Europe/Paris"),
                    country = "France",
                    countryCode = "FR",
                    forecastSource = id,
                    currentSource = id
                )
            )
        }
        arrayOf(
            MINUTELY_NO_PRECIPITATION,
            MINUTELY_ONE_PRECIPITATION,
            MINUTELY_TWO_PRECIPITATION,
            MINUTELY_THREE_PRECIPITATION,
            MINUTELY_INTERVAL_1,
            MINUTELY_INTERVAL_5,
            MINUTELY_INTERVAL_15
        ).forEachIndexed { index, label ->
            add(
                Location(
                    city = "$MINUTELY_CITY_LABEL$label",
                    latitude = MINUTELY_LATITUDE,
                    longitude = MINUTELY_LONGITUDE_START + index * 0.01, // 2.00, 2.01, 2.02, 2.03, etc
                    timeZone = TimeZone.getTimeZone("Europe/Paris"),
                    country = "France",
                    countryCode = "FR",
                    forecastSource = id,
                    minutelySource = id
                )
            )
        }
    }

    companion object {
        private const val CURRENT_LATITUDE = 48.0
        private const val CURRENT_LONGITUDE_START = 2.0
        private const val CURRENT_CITY_LABEL = "Current weather: "

        private const val MINUTELY_LATITUDE = 49.0
        private const val MINUTELY_LONGITUDE_START = 2.0
        private const val MINUTELY_CITY_LABEL = "Nowcasting: "
        private const val MINUTELY_NO_PRECIPITATION = "No precipitation"
        private const val MINUTELY_ONE_PRECIPITATION = "One minutely of precipitation"
        private const val MINUTELY_TWO_PRECIPITATION = "Two minutely of precipitation"
        private const val MINUTELY_THREE_PRECIPITATION = "Three minutely of precipitation"
        private const val MINUTELY_INTERVAL_1 = "1 minute interval"
        private const val MINUTELY_INTERVAL_5 = "5 minutes interval"
        private const val MINUTELY_INTERVAL_15 = "15 minutes interval"
    }
}

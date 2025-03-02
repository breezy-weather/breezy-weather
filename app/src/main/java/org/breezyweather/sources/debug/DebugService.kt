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
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.source.WeatherSource
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

class DebugService @Inject constructor() : WeatherSource {

    override val id = "debug"
    override val name = "Debug"

    private val weatherAttribution = "Debug"
    override val supportedFeatures = mapOf(
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
                    minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                        getMinutelyList(location)
                    } else {
                        null
                    }
                )
            )
        }
    }

    /**
     * To test the minutely source with different results, add:
     * - A location in Germany, for a minutely list with no precipitation
     * - A location in France, for a single item in the minutely list (crash testing)
     * - A location in Spain, for a two items in the minutely list (crash testing)
     * - A location in Great Britain, for a three items in the minutely list (crash testing)
     * - Any other location for a normal minutely list
     *
     * TODO: Add other kind of locations for testing different minute intervals
     */
    private fun getMinutelyList(location: Location): List<Minutely> {
        return with(location.countryCode) {
            when {
                equals("DE", ignoreCase = true) -> listOf(Minutely(Date(), 5, null))
                equals("FR", ignoreCase = true) -> generateMinutelyList(1)
                equals("ES", ignoreCase = true) -> generateMinutelyList(2)
                equals("GB", ignoreCase = true) -> generateMinutelyList(3)
                else -> generateMinutelyList(10)
            }
        }
    }

    private fun generateMinutelyList(times: Int): List<Minutely> {
        val interval = 15
        val currentDate = Date()
        return buildList {
            add(Minutely(currentDate, interval, Random.nextDouble().times(20)))
            if (times > 1) {
                for (i in 1..<times) {
                    val date = Date(currentDate.time + (i * interval).minutes.inWholeMilliseconds)
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
                            }
                        )
                    )
                }
            }
        }
    }

    // TODO
    override val testingLocations: List<Location> = emptyList()
}

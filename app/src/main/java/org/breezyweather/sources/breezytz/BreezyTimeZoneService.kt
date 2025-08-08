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

package org.breezyweather.sources.breezytz

import android.content.Context
import breezyweather.domain.location.model.Location
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.source.TimeZoneSource
import java.util.TimeZone
import javax.inject.Inject

/**
 * Offline timezone service
 * TODO: See #2093
 */
class BreezyTimeZoneService @Inject constructor() : TimeZoneSource {

    override val id = "breezytz"
    override val name = "Breezy Time Zone"

    override fun requestTimezone(
        context: Context,
        location: Location,
    ): Observable<TimeZone> {
        return Observable.just(
            if (!location.countryCode.isNullOrEmpty()) {
                // TODO
                TimeZone.getDefault()
            } else {
                // Fallback to ocean zones
                // See also https://github.com/eggert/tz/blob/2025b/etcetera#L37-L43
                when (location.longitude) {
                    in 172.5..180.0 -> TimeZone.getTimeZone("Etc/GMT-12")
                    in 157.5..172.5 -> TimeZone.getTimeZone("Etc/GMT-11")
                    in 142.5..157.5 -> TimeZone.getTimeZone("Etc/GMT-10")
                    in 127.5..142.5 -> TimeZone.getTimeZone("Etc/GMT-9")
                    in 112.5..127.5 -> TimeZone.getTimeZone("Etc/GMT-8")
                    in 97.5..112.5 -> TimeZone.getTimeZone("Etc/GMT-7")
                    in 82.5..97.5 -> TimeZone.getTimeZone("Etc/GMT-6")
                    in 67.5..82.5 -> TimeZone.getTimeZone("Etc/GMT-5")
                    in 52.5..67.5 -> TimeZone.getTimeZone("Etc/GMT-4")
                    in 37.5..52.5 -> TimeZone.getTimeZone("Etc/GMT-3")
                    in 22.5..37.5 -> TimeZone.getTimeZone("Etc/GMT-2")
                    in 7.5..22.5 -> TimeZone.getTimeZone("Etc/GMT-1")
                    in -7.5..7.5 -> TimeZone.getTimeZone("Etc/GMT")
                    in -22.5..-7.5 -> TimeZone.getTimeZone("Etc/GMT+1")
                    in -37.5..-22.5 -> TimeZone.getTimeZone("Etc/GMT+2")
                    in -52.5..-37.5 -> TimeZone.getTimeZone("Etc/GMT+3")
                    in -67.5..-52.5 -> TimeZone.getTimeZone("Etc/GMT+4")
                    in -82.5..-67.5 -> TimeZone.getTimeZone("Etc/GMT+5")
                    in -97.5..-82.5 -> TimeZone.getTimeZone("Etc/GMT+6")
                    in -112.5..-97.5 -> TimeZone.getTimeZone("Etc/GMT+7")
                    in -127.5..-112.5 -> TimeZone.getTimeZone("Etc/GMT+8")
                    in -142.5..-127.5 -> TimeZone.getTimeZone("Etc/GMT+9")
                    in -157.5..-142.5 -> TimeZone.getTimeZone("Etc/GMT+10")
                    in -172.5..-157.5 -> TimeZone.getTimeZone("Etc/GMT+11")
                    in -180.0..-172.5 -> TimeZone.getTimeZone("Etc/GMT+12")
                    else -> TimeZone.getDefault()
                }
            }
        )
    }
}

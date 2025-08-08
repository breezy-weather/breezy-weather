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
        return Observable.just(TimeZone.getDefault())
    }
}

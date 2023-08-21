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

package org.breezyweather.remoteviews

import android.content.Context
import org.breezyweather.common.basic.models.Location
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.remoteviews.gadgetbridge.GadgetBridgeService

object Gadgets {

    fun updateGadgetIfNecessary(context: Context) {
        val locationList = LocationEntityRepository.readLocationList().toMutableList()
        if (locationList.isNotEmpty()) {
            updateGadgetIfNecessary(context, locationList[0].copy(weather = WeatherEntityRepository.readWeather(locationList[0])))
        }
    }

    fun updateGadgetIfNecessary(context: Context, location: Location) {
        if (GadgetBridgeService.isEnabled(context)) {
            GadgetBridgeService.sendWeatherBroadcast(context, location);
        }
    }

}

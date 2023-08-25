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

package org.breezyweather.background.interfaces

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.main.MainActivity
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory

/**
 * Tile service.
 * TODO: Memory leak
 */
@RequiresApi(api = Build.VERSION_CODES.N)
class TileService : TileService() {
    override fun onTileAdded() {
        refreshTile(this, qsTile)
    }

    override fun onTileRemoved() {
        // do nothing.
    }

    override fun onStartListening() {
        refreshTile(this, qsTile)
    }

    override fun onStopListening() {
        refreshTile(this, qsTile)
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        val intent = Intent(MainActivity.ACTION_MAIN)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            this.startActivityAndCollapse(intent)
        }
    }

    companion object {
        private fun refreshTile(context: Context, tile: Tile?) {
            if (tile == null) return
            val location = LocationEntityRepository.readLocationList().getOrNull(0) ?: return
            val locationRefreshed = location.copy(weather = WeatherEntityRepository.readWeather(location))
            if (locationRefreshed.weather?.current != null) {
                tile.apply {
                    locationRefreshed.weather.current.weatherCode?.let {
                        icon = ResourceHelper.getMinimalIcon(
                            ResourcesProviderFactory.newInstance,
                            it,
                            locationRefreshed.isDaylight
                        )
                    }
                    tile.label = locationRefreshed.weather.current.temperature?.getTemperature(
                        context, SettingsManager.getInstance(context).temperatureUnit
                    )
                    state = Tile.STATE_INACTIVE
                }
                tile.updateTile()
            }
        }
    }
}
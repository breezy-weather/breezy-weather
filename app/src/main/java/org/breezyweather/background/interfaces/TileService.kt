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
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.ResourcesProviderFactory
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Tile service.
 * TODO: Memory leak
 */
@AndroidEntryPoint
@RequiresApi(api = Build.VERSION_CODES.N)
class TileService : TileService(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

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
            @Suppress("DEPRECATION")
            this.startActivityAndCollapse(intent)
        }
    }

    private fun refreshTile(context: Context, tile: Tile?) {
        if (tile == null) return
        launch {
            val location = locationRepository.getFirstLocation(withParameters = false) ?: return@launch
            val locationRefreshed = location.copy(
                weather = weatherRepository.getWeatherByLocationId(
                    location.formattedId,
                    withDaily = true, // isDaylight
                    withHourly = false,
                    withMinutely = false,
                    withAlerts = false,
                    withNormals = false
                )
            )
            locationRefreshed.weather?.current?.let { current ->
                tile.apply {
                    current.weatherCode?.let {
                        icon = ResourceHelper.getMinimalIcon(
                            ResourcesProviderFactory.newInstance,
                            it,
                            locationRefreshed.isDaylight
                        )
                    }
                    tile.label = current.temperature?.temperature?.formatMeasure(context)
                    state = Tile.STATE_INACTIVE
                }
                tile.updateTile()
            }
        }
    }
}

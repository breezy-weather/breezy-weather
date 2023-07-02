package org.breezyweather.background.interfaces

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import org.breezyweather.common.basic.models.Location.Companion.copy
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.db.repositories.LocationEntityRepository.readLocationList
import org.breezyweather.db.repositories.WeatherEntityRepository.readWeather
import org.breezyweather.settings.SettingsManager.Companion.getInstance
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.ResourcesProviderFactory.newInstance

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

    @SuppressLint("WrongConstant")
    override fun onClick() {
        try {
            val statusBarManager = getSystemService("statusbar")
            statusBarManager
                .javaClass
                .getMethod("collapsePanels")
                .invoke(statusBarManager)
        } catch (ignored: Exception) {
            // Not working anymore since API >= 31
        }
        IntentHelper.startMainActivity(this)
    }

    companion object {
        private fun refreshTile(context: Context, tile: Tile?) {
            if (tile == null) return
            val location = readLocationList(context).getOrNull(0) ?: return
            val locationRefreshed = location.copy(weather = readWeather(location))
            if (locationRefreshed.weather?.current != null) {
                tile.apply {
                    icon = ResourceHelper.getMinimalIcon(
                        newInstance,
                        locationRefreshed.weather.current.weatherCode,
                        locationRefreshed.isDaylight
                    )
                    tile.label = locationRefreshed.weather.current.temperature?.getTemperature(
                        context,
                        getInstance(context).temperatureUnit
                    )
                    state = Tile.STATE_INACTIVE
                }
                tile.updateTile()
            }
        }
    }
}
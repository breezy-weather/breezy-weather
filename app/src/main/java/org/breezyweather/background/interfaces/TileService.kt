package org.breezyweather.background.interfaces

import android.annotation.SuppressLint
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

    @SuppressLint("WrongConstant")
    override fun onClick() {
        this.startActivityAndCollapse(
            Intent(MainActivity.ACTION_MAIN)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    companion object {
        private fun refreshTile(context: Context, tile: Tile?) {
            if (tile == null) return
            val location = LocationEntityRepository.readLocationList(context).getOrNull(0) ?: return
            val locationRefreshed = location.copy(weather = WeatherEntityRepository.readWeather(location))
            if (locationRefreshed.weather?.current != null) {
                tile.apply {
                    icon = ResourceHelper.getMinimalIcon(
                        ResourcesProviderFactory.newInstance,
                        locationRefreshed.weather.current.weatherCode,
                        locationRefreshed.isDaylight
                    )
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
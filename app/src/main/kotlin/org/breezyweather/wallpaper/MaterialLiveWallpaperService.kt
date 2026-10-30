/*
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

package org.breezyweather.wallpaper

import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.reference.WeatherCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.powerManager
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor.Companion.FRAME_RATE_HIGH
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor.Companion.FRAME_RATE_LOW
import javax.inject.Inject

@AndroidEntryPoint
class MaterialLiveWallpaperService : WallpaperService() {
    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var firstLocationLoading: Job? = null

    override fun onCreate() {
        super.onCreate()
        firstLocation.value = null
        isRunning = true
    }

    override fun onCreateEngine(): Engine {
        return WeatherEngine()
    }

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        firstLocation.value = null
        super.onDestroy()
    }

    /**
     * Only needed when the wallpaper starts using the first location, as updates are pushed afterward with
     * [updateFirstLocation].
     */
    private fun loadFirstLocationIfNecessary() {
        if (firstLocation.value != null || firstLocationLoading?.isActive == true) return

        firstLocationLoading = scope.launch {
            val location = locationRepository.getFirstLocation(withParameters = false)?.let {
                it.copy(
                    weather = weatherRepository.getWeatherByLocationId(
                        it.formattedId,
                        withDaily = true, // For day/night
                        withHourly = false,
                        withMinutely = false,
                        withAlerts = false,
                        withNormals = false
                    )
                )
            }
            firstLocation.compareAndSet(null, FirstLocation(location))
        }
    }

    private inner class WeatherEngine : Engine() {

        private lateinit var renderer: LiveWallpaperRenderer
        private var visible = false

        @Volatile
        private var scene: LiveWallpaperRenderer.Scene? = null

        // Computing colors renders the background on a bitmap and analyzes it: only do it once per background
        @Volatile
        private var colors: Pair<LiveWallpaperRenderer.Scene, WallpaperColors?>? = null

        private val powerSaveModeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateScene()
            }
        }
        private var thermalStatusListener: PowerManager.OnThermalStatusChangedListener? = null

        // In case dark mode changed
        private val componentCallbacks = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                updateScene()
            }

            @Deprecated("Deprecated in Java")
            override fun onLowMemory() = Unit
        }
        private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            // Backgrounds are opaque, so the system doesn't need to blend the wallpaper
            surfaceHolder.setFormat(PixelFormat.RGBX_8888)
            // Not used, and they would be sent on every frame of home screen scrolling
            setOffsetNotificationsEnabled(false)

            val displayContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) displayContext else null
            renderer = LiveWallpaperRenderer(
                this@MaterialLiveWallpaperService,
                ContextCompat.getDisplayOrDefault(displayContext ?: this@MaterialLiveWallpaperService),
                surfaceHolder
            )
            registerComponentCallbacks(componentCallbacks)
            mainScope.launch { firstLocation.collect { updateScene() } }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            renderer.onSurfaceCreated()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderer.onSurfaceChanged(width, height)
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            // The system shows the wallpaper as soon as this returns: make sure the first frame shows the right weather
            firstLocationLoading?.let {
                if (it.isActive) {
                    runBlocking { withTimeoutOrNull(FIRST_LOCATION_TIMEOUT_MILLIS) { it.join() } }
                    updateScene()
                }
            }
            renderer.drawNow()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            renderer.onSurfaceDestroyed()
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (this.visible == visible) return

            this.visible = visible
            if (visible) {
                registerPowerListeners()
                // Cheap, everything is in memory
                updateScene()
            } else {
                unregisterPowerListeners()
            }
            renderer.setVisible(visible)
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors? {
            val scene = scene ?: return null
            colors?.let { (colorsScene, cachedColors) ->
                if (colorsScene.backgroundId == scene.backgroundId && colorsScene.darkMode == scene.darkMode) {
                    return cachedColors
                }
            }
            // Use a new drawable, as computing colors changes its bounds and it would affect the renderer
            return ResourcesCompat.getDrawable(resources, scene.backgroundId, null)
                ?.let { WallpaperColors.fromDrawable(it) }
                .also { colors = scene to it }
        }

        override fun onDestroy() {
            mainScope.cancel()
            unregisterComponentCallbacks(componentCallbacks)
            if (visible) {
                unregisterPowerListeners()
            }
            renderer.release()
            super.onDestroy()
        }

        fun updateScene() {
            val context = this@MaterialLiveWallpaperService
            val configManager = LiveWallpaperConfigManager(context)
            val location = if (configManager.isUsingFirstLocation) {
                loadFirstLocationIfNecessary()
                firstLocation.value?.location
            } else {
                null
            }
            val weatherKind = WeatherViewController.getWeatherKind(
                when (configManager.weatherKind) {
                    "auto" -> location?.weather?.current?.weatherCode
                    else -> WeatherCode.getInstance(configManager.weatherKind)
                }
            )
            val daytime = when (configManager.dayNightType) {
                "day" -> true
                "night" -> false
                else -> location?.isDaylight ?: true
            }
            val (warm, hot) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val thermalStatus = context.powerManager.currentThermalStatus
                Pair(
                    thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE,
                    thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
                )
            } else {
                Pair(false, false)
            }

            val newScene = LiveWallpaperRenderer.Scene(
                weatherKind = weatherKind,
                daytime = daytime,
                darkMode = context.isDarkMode,
                // Like other visual effects, stopped to save battery or to let the device cool down
                animate = configManager.animationsEnabled && !context.powerManager.isPowerSaveMode && !hot,
                gravitySensorEnabled = SettingsManager.getInstance(context).isGravitySensorEnabled,
                maxFrameRate = if (warm) FRAME_RATE_LOW else FRAME_RATE_HIGH
            )
            val oldScene = scene
            if (newScene == oldScene) return

            scene = newScene
            renderer.setScene(newScene)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 &&
                (
                    oldScene == null ||
                        oldScene.backgroundId != newScene.backgroundId ||
                        oldScene.darkMode != newScene.darkMode
                    )
            ) {
                notifyColorsChanged()
            }
        }

        private fun registerPowerListeners() {
            val context = this@MaterialLiveWallpaperService
            ContextCompat.registerReceiver(
                context,
                powerSaveModeReceiver,
                IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PowerManager.OnThermalStatusChangedListener { updateScene() }.also {
                    thermalStatusListener = it
                    context.powerManager.addThermalStatusListener(ContextCompat.getMainExecutor(context), it)
                }
            }
        }

        private fun unregisterPowerListeners() {
            val context = this@MaterialLiveWallpaperService
            context.unregisterReceiver(powerSaveModeReceiver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                thermalStatusListener?.let { context.powerManager.removeThermalStatusListener(it) }
                thermalStatusListener = null
            }
        }
    }

    companion object {
        private const val FIRST_LOCATION_TIMEOUT_MILLIS = 500L

        // The service only runs while the wallpaper is set on the home or lock screen, or is being previewed
        @Volatile
        var isRunning = false
            private set

        private data class FirstLocation(val location: Location?)

        // The first location, with its current weather and its daily forecast for day/night, kept in memory so that
        // the database doesn't need to be queried every time the wallpaper becomes visible (every time the user goes
        // back to the home screen or turns the screen on)
        private val firstLocation = MutableStateFlow<FirstLocation?>(null)

        /**
         * Pushes the latest data of the first location to the live wallpaper, if it is running.
         * To be called every time the first location or its weather changes.
         * Can be called from any thread.
         */
        fun updateFirstLocation(location: Location?) {
            if (!isRunning) return

            // Only keep what the wallpaper needs, as it can stay in memory for a long time
            firstLocation.value = FirstLocation(
                location?.copy(
                    weather = location.weather?.copy(
                        hourlyForecast = emptyList(),
                        minutelyForecast = emptyList(),
                        alertList = emptyList(),
                        normals = emptyMap()
                    )
                )
            )
        }
    }
}

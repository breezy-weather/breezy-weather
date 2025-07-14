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

package org.breezyweather.wallpaper

import android.app.WallpaperColors
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.service.wallpaper.WallpaperService
import android.view.OrientationEventListener
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.WeatherCode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import org.breezyweather.BreezyWeather
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.extensions.sensorManager
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.domain.location.model.isDaylight
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.ui.theme.weatherView.materialWeatherView.DelayRotateController
import org.breezyweather.ui.theme.weatherView.materialWeatherView.IntervalComputer
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView
import org.breezyweather.ui.theme.weatherView.materialWeatherView.WeatherImplementorFactory
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@AndroidEntryPoint
class MaterialLiveWallpaperService : WallpaperService() {
    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private enum class DeviceOrientation {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
    }

    override fun onCreateEngine(): Engine {
        return WeatherEngine(locationRepository, weatherRepository)
    }

    private inner class WeatherEngine(
        private val locationRepository: LocationRepository,
        private val weatherRepository: WeatherRepository,
    ) : Engine() {

        private var mHolder: SurfaceHolder? = null
        private var mIntervalComputer: IntervalComputer? = null
        private var mRotators: Array<MaterialWeatherView.RotateController>? = null
        private var mImplementor: MaterialWeatherView.WeatherAnimationImplementor? = null
        private var mBackground: Drawable? = null
        private var mOpenGravitySensor = false
        private var mGravitySensor: Sensor? = null

        @Size(2)
        private var mSizes: IntArray = intArrayOf(0, 0)

        @Size(2)
        private var mAdaptiveSize: IntArray = intArrayOf(0, 0)
        private var mRotation2D = 0f
        private var mRotation3D = 0f

        @WeatherKindRule
        private var mWeatherKind = 0
        private var mDaytime = false
        private var mVisible = false
        private var mAnimate = false
        private var hasDrawn = false
        private var mDeviceOrientation: DeviceOrientation = DeviceOrientation.TOP
        private var mIntervalController: AsyncHelper.Controller? = null
        private var mHandlerThread: HandlerThread? = null
        private var mHandler: Handler? = null
        private val mDrawableRunnable = Runnable {
            if (mImplementor == null ||
                mBackground == null ||
                mRotators == null ||
                mHandler == null
            ) {
                return@Runnable
            }
            // LogHelper.log(msg = "[LiveWallpaper] Runnable is running")
            mIntervalComputer?.invalidate()
            if (mRotators != null && mIntervalComputer != null) {
                mRotators!![0].updateRotation(mRotation2D.toDouble(), mIntervalComputer!!.interval)
                mRotators!![1].updateRotation(mRotation3D.toDouble(), mIntervalComputer!!.interval)
            }

            try {
                mHolder?.lockCanvas()?.let { canvas ->
                    if (mSizes[0] != canvas.width || mSizes[1] != canvas.height) {
                        mSizes[0] = canvas.width
                        mSizes[1] = canvas.height
                        mAdaptiveSize[0] = applicationContext.getTabletListAdaptiveWidth(mSizes[0])
                        mAdaptiveSize[1] = mSizes[1]
                        mBackground?.setBounds(0, 0, mSizes[0], mSizes[1])
                    }
                    mBackground?.draw(canvas)
                    if (mIntervalComputer != null && mRotators != null) {
                        var interval = mIntervalComputer!!.interval
                        if (!mAnimate) {
                            if (hasDrawn) {
                                interval = 0.0
                            } else {
                                hasDrawn = true
                            }
                        }
                        mImplementor?.updateData(
                            mAdaptiveSize,
                            interval.toLong(),
                            mRotators!![0].rotation.toFloat(),
                            mRotators!![1].rotation.toFloat()
                        )
                    }
                    if (mImplementor != null && mRotators != null) {
                        canvas.withTranslation(
                            (mSizes[0] - mAdaptiveSize[0]) / 2f,
                            (mSizes[1] - mAdaptiveSize[1]) / 2f
                        ) {
                            mImplementor!!.draw(
                                mAdaptiveSize,
                                this,
                                0f,
                                mRotators!![0].rotation.toFloat(),
                                mRotators!![1].rotation.toFloat()
                            )
                        }
                    }
                    mHolder?.unlockCanvasAndPost(canvas)
                }
            } catch (e: Throwable) {
                if (BreezyWeather.instance.debugMode) {
                    e.printStackTrace()
                }
            }
        }

        private val mGravityListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(ev: SensorEvent) {
                // x : (+) fall to the left / (-) fall to the right.
                // y : (+) stand / (-) head stand.
                // z : (+) look down / (-) look up.
                // rotation2D : (+) anticlockwise / (-) clockwise.
                // rotation3D : (+) look down / (-) look up.
                if (mOpenGravitySensor) {
                    val aX = ev.values[0]
                    val aY = ev.values[1]
                    val aZ = ev.values[2]
                    val g2D = sqrt((aX * aX + aY * aY).toDouble())
                    val g3D = sqrt((aX * aX + aY * aY + aZ * aZ).toDouble())
                    val cos2D = max(min(1.0, aY / g2D), -1.0)
                    val cos3D = max(min(1.0, g2D * (if (aY >= 0) 1 else -1) / g3D), -1.0)
                    mRotation2D = Math.toDegrees(acos(cos2D)).toFloat() * if (aX >= 0) 1 else -1
                    mRotation3D = Math.toDegrees(acos(cos3D)).toFloat() * if (aZ >= 0) 1 else -1
                    when (mDeviceOrientation) {
                        DeviceOrientation.TOP -> {}
                        DeviceOrientation.LEFT -> mRotation2D -= 90f
                        DeviceOrientation.RIGHT -> mRotation2D += 90f
                        DeviceOrientation.BOTTOM -> if (mRotation2D > 0) {
                            mRotation2D -= 180f
                        } else {
                            mRotation2D += 180f
                        }
                    }
                    if (60 < abs(mRotation3D) && abs(mRotation3D) < 120) {
                        mRotation2D *= (abs(abs(mRotation3D) - 90) / 30.0).toFloat()
                    }
                } else {
                    mRotation2D = 0f
                    mRotation3D = 0f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {
                // do nothing.
            }
        }
        private val mOrientationListener: OrientationEventListener =
            object : OrientationEventListener(
                applicationContext
            ) {
                override fun onOrientationChanged(orientation: Int) {
                    val newOrientation = getDeviceOrientation(orientation)
                    if (newOrientation != mDeviceOrientation) {
                        // LogHelper.log(msg = "[LiveWallpaper] Orientation: $mDeviceOrientation -> $newOrientation")
                        mDeviceOrientation = newOrientation
                        if (!mAnimate) {
                            mHandler?.post(mDrawableRunnable)
                        }
                    }
                }

                private fun getDeviceOrientation(orientation: Int): DeviceOrientation {
                    return if (applicationContext.isLandscape) {
                        if (0 < orientation && orientation < 180) DeviceOrientation.RIGHT else DeviceOrientation.LEFT
                    } else {
                        if (270 < orientation || orientation < 90) DeviceOrientation.TOP else DeviceOrientation.BOTTOM
                    }
                }
            }

        private fun setWeather(@WeatherKindRule weatherKind: Int, daytime: Boolean) {
            mWeatherKind = weatherKind
            mDaytime = daytime
        }

        private fun setWeatherImplementor() {
            hasDrawn = false
            mImplementor = WeatherImplementorFactory.getWeatherImplementor(
                mWeatherKind,
                mDaytime,
                mAdaptiveSize,
                mAnimate
            )
            mRotators = arrayOf(
                DelayRotateController(mRotation2D.toDouble()),
                DelayRotateController(mRotation3D.toDouble())
            )
        }

        private fun setWeatherBackgroundDrawable() {
            mBackground = ResourcesCompat.getDrawable(
                resources,
                WeatherImplementorFactory.getBackgroundId(mWeatherKind, mDaytime),
                null
            )
            mBackground?.let {
                it.setBounds(0, 0, mSizes[0], mSizes[1])
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    notifyColorsChanged()
                }
            }
        }

        private fun setIntervalComputer() {
            // Disable animations
            /*mIntervalComputer?.reset() ?: run {
                mIntervalComputer = IntervalComputer()
            }*/
        }

        private fun setOpenGravitySensor(openGravitySensor: Boolean) {
            mOpenGravitySensor = openGravitySensor
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            mDeviceOrientation = DeviceOrientation.TOP
            mHandlerThread = HandlerThread(
                System.currentTimeMillis().toString(),
                Process.THREAD_PRIORITY_FOREGROUND
            ).also {
                it.start()
            }.also {
                mHandler = Handler(it.looper)
            }
            mSizes = intArrayOf(0, 0)
            mAdaptiveSize = intArrayOf(0, 0)
            mHolder = surfaceHolder.apply {
                addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {}
                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int,
                    ) {
                        if (holder.surface.isValid) {
                            mSizes[0] = width
                            mSizes[1] = height
                            mAdaptiveSize[0] =
                                applicationContext.getTabletListAdaptiveWidth(mSizes[0])
                            mAdaptiveSize[1] = mSizes[1]
                            mBackground?.setBounds(0, 0, mSizes[0], mSizes[1])
                            // Animations disabled: see #1006, #1325
                            // Possible way to bring it back using shaders: #1665
                            /*mAnimate =
                                when (SettingsManager.getInstance(applicationContext).backgroundAnimationMode) {
                                    BackgroundAnimationMode.SYSTEM -> !applicationContext.isMotionReduced
                                    BackgroundAnimationMode.ENABLED -> true
                                    BackgroundAnimationMode.DISABLED -> false
                                }*/
                            setWeatherImplementor()
                        }
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {}
                })
                setFormat(PixelFormat.RGBA_8888)
            }
            sensorManager?.let {
                mOpenGravitySensor = true
                mGravitySensor = it.getDefaultSensor(Sensor.TYPE_GRAVITY)
            }
            mVisible = false
            setWeather(WeatherView.WEATHER_KIND_NULL, true)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (mVisible == visible) return

            mVisible = visible
            if (!visible) {
                mIntervalController?.let {
                    it.cancel()
                    mIntervalController = null
                }
                mHandler?.removeCallbacksAndMessages(null)
                sensorManager?.unregisterListener(mGravityListener, mGravitySensor)
                mOrientationListener.disable()
                return
            }

            val settingsManager = SettingsManager.getInstance(applicationContext)
            // Animations disabled: see #1006, #1325
            // Possible way to bring it back using shaders: #1665
            /*mAnimate =
                when (settingsManager.backgroundAnimationMode) {
                    BackgroundAnimationMode.SYSTEM -> !applicationContext.isMotionReduced
                    BackgroundAnimationMode.ENABLED -> true
                    BackgroundAnimationMode.DISABLED -> false
                }*/
            mRotation2D = 0f
            mRotation3D = 0f
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable()
            }
            val configManager = LiveWallpaperConfigManager(this@MaterialLiveWallpaperService)

            val location: Location? = if (configManager.weatherKind == "auto" || configManager.dayNightType == "auto") {
                // TODO: Isn't there a more efficient way than reloading the location from database
                // everytime the visibility changes??
                // TODO
                runBlocking {
                    locationRepository.getFirstLocation(withParameters = false)
                        .let {
                            it?.copy(
                                weather = weatherRepository.getWeatherByLocationId(
                                    it.formattedId,
                                    withDaily = configManager.dayNightType == "auto",
                                    withHourly = false,
                                    withMinutely = false,
                                    withAlerts = false
                                )
                            )
                        }
                }
            } else {
                null
            }
            val weatherKind = when (configManager.weatherKind) {
                "auto" -> location?.weather?.current?.weatherCode
                else -> WeatherCode.getInstance(configManager.weatherKind)
            }
            val daytime = when (configManager.dayNightType) {
                "day" -> true
                "night" -> false
                else -> location?.isDaylight ?: true
            }
            setWeather(
                WeatherViewController.getWeatherKind(weatherKind),
                daytime
            )

            setWeatherImplementor()
            setIntervalComputer()
            setOpenGravitySensor(settingsManager.isGravitySensorEnabled)
            if (mOpenGravitySensor) {
                sensorManager?.registerListener(
                    mGravityListener,
                    mGravitySensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            } else {
                sensorManager?.unregisterListener(mGravityListener, mGravitySensor)
            }

            setWeatherBackgroundDrawable()
            if (mAnimate) {
                val screenRefreshRate = ContextCompat.getDisplayOrDefault(this@MaterialLiveWallpaperService)
                    .refreshRate.let {
                        if (it > 60f) 60f else it
                    }
                mIntervalController = AsyncHelper.intervalRunOnUI(
                    { mHandler?.post(mDrawableRunnable) },
                    (1000.0 / screenRefreshRate).toLong(),
                    0
                )
            } else {
                mHandler?.post(mDrawableRunnable)
                // Run again 1 sec later in case the canvas size was not correctly set the first time on preview screen
                AsyncHelper.delayRunOnUI(
                    { mHandler?.post(mDrawableRunnable) },
                    1000
                )
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors? {
            return if (mBackground != null) {
                WallpaperColors.fromDrawable(mBackground)
            } else {
                null
            }
        }

        override fun onDestroy() {
            onVisibilityChanged(false)
            mHandlerThread?.quit()
        }
    }
}

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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.Choreographer
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.DrawableRes
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import org.breezyweather.BreezyWeather
import org.breezyweather.ui.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.ui.theme.weatherView.materialWeatherView.DelayRotateController
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor.Companion.FRAME_RATE_HIGH
import org.breezyweather.ui.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor.Companion.FRAME_RATE_LOW
import org.breezyweather.ui.theme.weatherView.materialWeatherView.WeatherImplementorFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Draws the weather on the live wallpaper surface, from a dedicated thread.
 *
 * The wallpaper is visible most of the time the screen is on, so it is designed to use as little power as possible:
 * - frames are drawn with a hardware-accelerated canvas (the GPU rasterizes them instead of the CPU), in sync with the
 * display thanks to [Choreographer], and only as often as the animation needs it (see
 * [WeatherAnimationImplementor.preferredFrameRate]). This frame rate is also given to the system, so that it can lower
 * the display refresh rate
 * - nothing is drawn while the wallpaper is hidden. When the weather is not animated, only one frame is drawn when the
 * wallpaper becomes visible or when what it shows changes
 * - the gravity sensor and the display are only listened to while the weather is animated
 *
 * The drawing state is confined to the render thread: functions called from the main thread post their work to it.
 */
internal class LiveWallpaperRenderer(
    private val context: Context,
    private val display: Display,
    private val surfaceHolder: SurfaceHolder,
) {
    /**
     * What the wallpaper shows.
     * @param animate false to draw a still frame
     * @param maxFrameRate lowered when the device is getting hot
     */
    data class Scene(
        @WeatherKindRule val weatherKind: Int,
        val daytime: Boolean,
        val darkMode: Boolean,
        val animate: Boolean,
        val gravitySensorEnabled: Boolean,
        val maxFrameRate: Int,
    ) {
        @get:DrawableRes
        val backgroundId: Int
            get() = WeatherImplementorFactory.getBackgroundId(weatherKind, daytime)
    }

    private val thread = HandlerThread("LiveWallpaperRenderer", Process.THREAD_PRIORITY_FOREGROUND).apply { start() }
    private val handler = Handler(thread.looper)
    private lateinit var choreographer: Choreographer

    // Prevents the surface from being destroyed while a frame is drawn on it
    private val surfaceLock = Any()
    private var surfaceAvailable = false // Guarded by surfaceLock

    private var scene: Scene? = null
    private var background: Drawable? = null
    private var backgroundOpaque = false
    private var implementor: WeatherAnimationImplementor? = null
    private val canvasSize = intArrayOf(0, 0)
    private var visible = false
    private var animating = false
    private val intervalCounter = FrameIntervalCounter()
    private var fastFramesUntilNanos = 0L
    private var frameRateVote = NO_FRAME_RATE_VOTE

    private val displayManager: DisplayManager? = context.getSystemService()
    private var refreshRate = FramePacing.DEFAULT_REFRESH_RATE
    private var displayRotation = Surface.ROTATION_0

    private val sensorManager: SensorManager? = context.getSystemService()
    private val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private var listeningToGravity = false
    private var targetRotation2D = 0f
    private var targetRotation3D = 0f
    private var rotators = arrayOf(DelayRotateController(0.0), DelayRotateController(0.0))

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        if (animating) {
            drawFrame(intervalCounter.next(frameTimeNanos), frameTimeNanos)
            scheduleNextFrame(frameTimeNanos)
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == display.displayId) {
                updateDisplayInfo()
            }
        }
    }

    private val gravityListener = object : SensorEventListener {
        override fun onSensorChanged(ev: SensorEvent) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            val aX = ev.values[0]
            val aY = ev.values[1]
            val aZ = ev.values[2]
            val g2D = sqrt(aX * aX + aY * aY)
            val g3D = sqrt(aX * aX + aY * aY + aZ * aZ)
            if (g3D == 0f) return

            // When the device lies perfectly flat, there is no rotation around the screen axis (and no NaN)
            var rotation2D = if (g2D > 0f) {
                Math.toDegrees(acos((aY / g2D).coerceIn(-1f, 1f).toDouble())).toFloat() * if (aX >= 0) 1 else -1
            } else {
                0f
            }
            val rotation3D =
                Math.toDegrees(acos((g2D / g3D).coerceIn(-1f, 1f).toDouble())).toFloat() * if (aZ >= 0) 1 else -1

            // Gravity is given relative to the natural orientation of the device, while the wallpaper follows the
            // orientation of the screen
            when (displayRotation) {
                Surface.ROTATION_90 -> rotation2D -= 90f
                Surface.ROTATION_270 -> rotation2D += 90f
                Surface.ROTATION_180 -> rotation2D += if (rotation2D > 0) -180f else 180f
            }
            if (60 < abs(rotation3D) && abs(rotation3D) < 120) {
                rotation2D *= abs(abs(rotation3D) - 90) / 30f
            }
            targetRotation2D = rotation2D
            targetRotation3D = rotation3D
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    init {
        handler.post { choreographer = Choreographer.getInstance() }
    }

    fun setScene(scene: Scene) {
        handler.post { applyScene(scene) }
    }

    fun setVisible(visible: Boolean) {
        handler.post { applyVisibility(visible) }
    }

    fun onSurfaceCreated() {
        synchronized(surfaceLock) { surfaceAvailable = true }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        handler.post { resize(width, height) }
    }

    /**
     * Waits for the frame being drawn, if any, then stops drawing until the surface is created again.
     */
    fun onSurfaceDestroyed() {
        synchronized(surfaceLock) { surfaceAvailable = false }
    }

    /**
     * Draws a frame and waits for it, as the system shows the wallpaper as soon as it's drawn.
     */
    fun drawNow() {
        val drawn = CountDownLatch(1)
        val posted = handler.post {
            drawFrame(0L, System.nanoTime())
            drawn.countDown()
        }
        if (posted) {
            drawn.await(DRAW_NOW_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        }
    }

    fun release() {
        handler.post {
            applyVisibility(false)
            implementor = null
            background = null
        }
        thread.quitSafely()
    }

    private fun applyScene(newScene: Scene) {
        val oldScene = scene
        if (oldScene == newScene) return
        scene = newScene

        if (oldScene == null ||
            oldScene.backgroundId != newScene.backgroundId ||
            oldScene.darkMode != newScene.darkMode
        ) {
            background = ResourcesCompat.getDrawable(context.resources, newScene.backgroundId, null)?.apply {
                setBounds(0, 0, canvasSize[0], canvasSize[1])
            }
            // Only used to know if the canvas must be cleared, which getOpacity() still tells reliably
            @Suppress("DEPRECATION")
            val opacity = background?.opacity
            backgroundOpaque = opacity == PixelFormat.OPAQUE
        }
        if (oldScene == null ||
            oldScene.weatherKind != newScene.weatherKind ||
            oldScene.daytime != newScene.daytime ||
            oldScene.darkMode != newScene.darkMode ||
            oldScene.animate != newScene.animate
        ) {
            createImplementor()
        }
        updateAnimation()
        if (visible && !animating) {
            drawFrame(0L, System.nanoTime())
        }
    }

    private fun applyVisibility(visible: Boolean) {
        if (this.visible == visible) return
        this.visible = visible
        updateAnimation()
        // The previous frame is still there, but redraw it once as the weather may have changed while hidden
        if (visible && !animating) {
            drawFrame(0L, System.nanoTime())
        }
    }

    private fun resize(width: Int, height: Int) {
        // The surface may have been recreated, losing the frame rate given to it
        frameRateVote = NO_FRAME_RATE_VOTE
        if (canvasSize[0] == width && canvasSize[1] == height) return

        canvasSize[0] = width
        canvasSize[1] = height
        background?.setBounds(0, 0, width, height)
        // Animations are laid out according to the size of the canvas
        createImplementor()
        updateAnimation()
    }

    private fun createImplementor() {
        val scene = scene
        implementor = if (scene != null && canvasSize[0] > 0 && canvasSize[1] > 0) {
            WeatherImplementorFactory.getWeatherImplementor(
                context,
                scene.weatherKind,
                scene.daytime,
                canvasSize,
                scene.animate
            )
        } else {
            null
        }
        if (scene?.animate != true) {
            targetRotation2D = 0f
            targetRotation3D = 0f
        }
        rotators = arrayOf(
            DelayRotateController(targetRotation2D.toDouble()),
            DelayRotateController(targetRotation3D.toDouble())
        )
    }

    private fun updateAnimation() {
        val animate = visible && implementor != null && scene?.animate == true
        if (animate != animating) {
            animating = animate
            if (animate) {
                intervalCounter.reset()
                displayManager?.registerDisplayListener(displayListener, handler)
                updateDisplayInfo()
                choreographer.postFrameCallback(frameCallback)
            } else {
                choreographer.removeFrameCallback(frameCallback)
                displayManager?.unregisterDisplayListener(displayListener)
                voteFrameRate(0)
            }
        }
        updateGravitySensor()
    }

    private fun updateGravitySensor() {
        val listen = animating && scene?.gravitySensorEnabled == true && gravitySensor != null
        if (listen != listeningToGravity) {
            listeningToGravity = listen
            if (listen) {
                sensorManager!!.registerListener(gravityListener, gravitySensor, GRAVITY_SAMPLING_PERIOD_US, handler)
            } else {
                sensorManager?.unregisterListener(gravityListener)
            }
        }
        if (scene?.gravitySensorEnabled != true) {
            // Smoothly goes back to the default position
            targetRotation2D = 0f
            targetRotation3D = 0f
        }
    }

    private fun updateDisplayInfo() {
        refreshRate = display.refreshRate.takeIf { it > 0f } ?: FramePacing.DEFAULT_REFRESH_RATE
        displayRotation = display.rotation
    }

    private fun scheduleNextFrame(frameTimeNanos: Long) {
        val preferredFrameRate = if (frameTimeNanos < fastFramesUntilNanos) {
            FRAME_RATE_HIGH
        } else {
            implementor?.preferredFrameRate ?: FRAME_RATE_HIGH
        }
        val frameRate = min(preferredFrameRate, scene?.maxFrameRate ?: FRAME_RATE_HIGH)
        voteFrameRate(frameRate)

        val delayMillis = FramePacing.nextFrameDelayMillis(frameTimeNanos, System.nanoTime(), refreshRate, frameRate)
        if (delayMillis > 0) {
            choreographer.postFrameCallbackDelayed(frameCallback, delayMillis)
        } else {
            choreographer.postFrameCallback(frameCallback)
        }
    }

    /**
     * Tells the system how often the wallpaper is drawn, so that it can lower the display refresh rate when nothing
     * else needs a higher one. 0 removes the vote.
     */
    private fun voteFrameRate(frameRate: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || frameRate == frameRateVote) return

        synchronized(surfaceLock) {
            if (!surfaceAvailable) return
            try {
                surfaceHolder.surface.setFrameRate(frameRate.toFloat(), Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)
                frameRateVote = frameRate
            } catch (e: RuntimeException) {
                if (BreezyWeather.instance.debugMode) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawFrame(intervalMillis: Long, frameTimeNanos: Long) {
        synchronized(surfaceLock) {
            if (!surfaceAvailable || canvasSize[0] <= 0 || canvasSize[1] <= 0) return
            try {
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Rasterized by the GPU. A software canvas makes the CPU fill every pixel of every frame
                    surfaceHolder.lockHardwareCanvas()
                } else {
                    surfaceHolder.lockCanvas()
                } ?: return
                try {
                    drawScene(canvas, intervalMillis, frameTimeNanos)
                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            } catch (e: RuntimeException) {
                // The surface can be lost at any time
                if (BreezyWeather.instance.debugMode) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawScene(canvas: Canvas, intervalMillis: Long, frameTimeNanos: Long) {
        // Hardware canvases are not cleared between frames
        if (!backgroundOpaque) {
            canvas.drawColor(Color.BLACK)
        }
        background?.draw(canvas)

        val implementor = implementor ?: return
        val previousRotation2D = rotators[0].rotation
        val previousRotation3D = rotators[1].rotation
        rotators[0].updateRotation(targetRotation2D.toDouble(), intervalMillis.toDouble())
        rotators[1].updateRotation(targetRotation3D.toDouble(), intervalMillis.toDouble())
        val rotationSpeed = max(
            abs(rotators[0].rotation - previousRotation2D),
            abs(rotators[1].rotation - previousRotation3D)
        ) / intervalMillis.coerceAtLeast(1L)
        if (rotationSpeed > FAST_ROTATION_SPEED) {
            // The device is moving: parallax effect of the gravity sensor needs every frame to be smooth
            fastFramesUntilNanos = frameTimeNanos + FAST_FRAMES_AFTER_ROTATION_NANOS
        }

        val rotation2D = rotators[0].rotation.toFloat()
        val rotation3D = rotators[1].rotation.toFloat()
        implementor.updateData(canvasSize, intervalMillis, rotation2D, rotation3D)
        canvas.withSave {
            implementor.draw(canvasSize, this, 0f, rotation2D, rotation3D)
        }
    }

    companion object {
        private const val NO_FRAME_RATE_VOTE = -1
        private const val DRAW_NOW_TIMEOUT_MILLIS = 500L
        private const val GRAVITY_SAMPLING_PERIOD_US = 1_000_000 / FRAME_RATE_LOW

        // Degrees per millisecond. Above it, the parallax effect moves elements by about a pixel per frame or more at
        // low frame rate
        private const val FAST_ROTATION_SPEED = 0.001
        private const val FAST_FRAMES_AFTER_ROTATION_NANOS = 300 * FramePacing.NANOS_PER_MILLI
    }
}

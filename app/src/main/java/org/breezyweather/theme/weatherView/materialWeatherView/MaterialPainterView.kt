package org.breezyweather.theme.weatherView.materialWeatherView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.Size
import androidx.core.content.res.ResourcesCompat
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

@SuppressLint("ViewConstructor")
class MaterialPainterView(
    context: Context,

    @WeatherKindRule private var weatherKind: Int,
    private var daylight: Boolean,

    isDrawable: Boolean,
    currentScrollRate: Float,

    var gravitySensorEnabled: Boolean,
    var animatable: Boolean
) : View(context) {

    private var intervalComputer: IntervalComputer? = null

    private var impl: MaterialWeatherView.WeatherAnimationImplementor? = null
    private var rotators: Array<MaterialWeatherView.RotateController>? = null

    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null

    @Size(2)
    private var canvasSize = IntArray(2)
    private var rotation2D = 0f
    private var rotation3D = 0f

    @FloatRange(from = 0.0)
    private var lastScrollRate = 0f

    @FloatRange(from = 0.0)
    var scrollRate = 0f
        set(value) {
            field = value

            if (lastScrollRate >= 1 && field < 1) {
                postInvalidate()
            }
        }

    var drawable = false
        set(value) {
            if (field == value) {
                return
            }
            field = value

            if (value) {
                resetDrawer()
                return
            }

            sensorManager?.unregisterListener(mGravityListener, gravitySensor)
            orientationListener.disable()
        }

    private var drawnFrames = 0
    private val drawFramesLimit = 50

    private var mDeviceOrientation: DeviceOrientation? = null

    private enum class DeviceOrientation {
        TOP, LEFT, BOTTOM, RIGHT
    }

    private val mGravityListener: SensorEventListener = object : SensorEventListener {

        override fun onSensorChanged(ev: SensorEvent) {
            // x : (+) fall to the left / (-) fall to the right.
            // y : (+) stand / (-) head stand.
            // z : (+) look down / (-) look up.
            // rotation2D : (+) anticlockwise / (-) clockwise.
            // rotation3D : (+) look down / (-) look up.
            if (gravitySensorEnabled) {
                val aX = ev.values[0]
                val aY = ev.values[1]
                val aZ = ev.values[2]

                val g2D = sqrt((aX * aX + aY * aY).toDouble())
                val g3D = sqrt((aX * aX + aY * aY + aZ * aZ).toDouble())

                val cos2D = 1.0.coerceAtMost(aY / g2D).coerceAtLeast(-1.0)
                val cos3D = 1.0.coerceAtMost(g2D / g3D).coerceAtLeast(-1.0)

                rotation2D = Math.toDegrees(acos(cos2D)).toFloat() * if (aX >= 0) {
                    1
                } else {
                    -1
                }
                rotation3D = Math.toDegrees(acos(cos3D)).toFloat() * if (aZ >= 0) {
                    1
                } else {
                    -1
                }

                when (mDeviceOrientation) {
                    DeviceOrientation.TOP -> {
                        // do nothing.
                    }

                    DeviceOrientation.LEFT -> {
                        rotation2D -= 90f
                    }

                    DeviceOrientation.RIGHT -> {
                        rotation2D += 90f
                    }

                    DeviceOrientation.BOTTOM -> {
                        if (rotation2D > 0) {
                            rotation2D -= 180f
                        } else {
                            rotation2D += 180f
                        }
                    }

                    else -> {
                        // do nothing.
                    }
                }
                if (60 < abs(rotation3D) && abs(rotation3D) < 120) {
                    rotation2D *= (abs(abs(rotation3D) - 90) / 30.0).toFloat()
                }
            } else {
                rotation2D = 0f
                rotation3D = 0f
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {
            // do nothing.
        }
    }

    private val orientationListener: OrientationEventListener = object : OrientationEventListener(
        getContext()
    ) {
        override fun onOrientationChanged(orientation: Int) {
            mDeviceOrientation = getDeviceOrientation(orientation)
        }

        private fun getDeviceOrientation(orientation: Int): DeviceOrientation {
            return if (context.isLandscape) {
                if (orientation in 1..179) {
                    DeviceOrientation.RIGHT
                } else {
                    DeviceOrientation.LEFT
                }
            } else {
                if (270 < orientation || orientation < 90) {
                    DeviceOrientation.TOP
                } else {
                    DeviceOrientation.BOTTOM
                }
            }
        }
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

        val metrics = resources.displayMetrics
        canvasSize = intArrayOf(
            context.getTabletListAdaptiveWidth(metrics.widthPixels),
            metrics.heightPixels
        )

        drawable = isDrawable
        lastScrollRate = currentScrollRate
        scrollRate = currentScrollRate
        mDeviceOrientation = DeviceOrientation.TOP

        background = getWeatherBackgroundDrawable(weatherKind, daylight)
    }

    fun update(
        @WeatherKindRule weatherKind: Int,
        daylight: Boolean,
        gravitySensorEnabled: Boolean,
        animate: Boolean
    ) {
        this.weatherKind = weatherKind
        this.daylight = daylight
        this.gravitySensorEnabled = gravitySensorEnabled
        this.animatable = animate

        if (drawable) {
            setWeatherImplementor()
            setIntervalComputer()
            postInvalidate()
        }

        background = getWeatherBackgroundDrawable(weatherKind, daylight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (measuredWidth != 0 && measuredHeight != 0) {
            val width = context.getTabletListAdaptiveWidth(measuredWidth)
            val height = measuredHeight

            if (canvasSize[0] != width || canvasSize[1] != height) {
                canvasSize[0] = width
                canvasSize[1] = height
                setWeatherImplementor()
            }
        }
    }

    // this is inefficient for cases when animations are disabled,
    // as basic view clears canvas on each call, forcing us to redraw with same data
    // maybe use TextureView/SurfaceView or save to bitmap?
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (intervalComputer == null
            || rotators == null
            || impl == null
        ) {
            return
        }

        intervalComputer!!.invalidate()
        rotators!![0].updateRotation(rotation2D.toDouble(), intervalComputer!!.interval)
        rotators!![1].updateRotation(rotation3D.toDouble(), intervalComputer!!.interval)

        var interval = intervalComputer!!.interval
        if (!animatable) {
            interval -= drawnFrames.toDouble() / drawFramesLimit * interval
            if (drawnFrames < drawFramesLimit) drawnFrames += 1
        }

        impl!!.updateData(
            canvasSize,
            interval.toLong(),
            rotators!![0].rotation.toFloat(),
            rotators!![1].rotation.toFloat()
        )

        if (impl != null && rotators != null) {
            canvas.save()
            canvas.translate(
                (measuredWidth - canvasSize[0]) / 2f,
                (measuredHeight - canvasSize[1]) / 2f
            )
            impl!!.draw(
                canvasSize,
                canvas,
                scrollRate,
                rotators!![0].rotation.toFloat(),
                rotators!![1].rotation.toFloat()
            )
            canvas.restore()
        }

        if (!drawable) {
            return
        }
        if (lastScrollRate >= 1 && scrollRate >= 1) {
            lastScrollRate = scrollRate
            setIntervalComputer()
            return
        }
        lastScrollRate = scrollRate

        postInvalidate()
    }

    private fun resetDrawer() {
        rotation2D = 0f
        rotation3D = 0f

        sensorManager?.registerListener(
            mGravityListener,
            gravitySensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }

        setWeatherImplementor()
        setIntervalComputer()
        postInvalidate()
    }

    private fun getWeatherBackgroundDrawable(
        weatherKind: Int,
        daylight: Boolean,
    ) = ResourcesCompat.getDrawable(
        resources,
        WeatherImplementorFactory.getBackgroundId(weatherKind, daylight),
        null
    )

    private fun setWeatherImplementor() {
        drawnFrames = 0
        impl = WeatherImplementorFactory.getWeatherImplementor(
            weatherKind,
            daylight,
            canvasSize,
            animatable
        )
        rotators = arrayOf(
            DelayRotateController(
                rotation2D.toDouble()
            ),
            DelayRotateController(
                rotation3D.toDouble()
            )
        )
    }

    private fun setIntervalComputer() {
        if (intervalComputer == null) {
            intervalComputer =
                IntervalComputer()
        } else {
            intervalComputer!!.reset()
        }
    }
}
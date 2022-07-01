package wangdaye.com.geometricweather.common.ui.widgets.precipitationBar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import wangdaye.com.geometricweather.common.ui.widgets.DayNightShaderWrapper
import wangdaye.com.geometricweather.common.utils.DisplayUtils

private const val POLYLINE_SIZE_DIP = 3.5f
private const val SHADOW_ALPHA_FACTOR_LIGHT = 0.2f
private const val SHADOW_ALPHA_FACTOR_DARK = 0.4f

data class PolylineKeyPoint(
    val originCenterX: Float,
    val originTopY: Float,
)

class PrecipitationBarBackgroundView(context: Context) : View(context) {

    // public data.

    var values = emptyArray<Double>()
        set(value) {
            field = value
            requestLayout()
        }

    @ColorInt
    var precipitationColor = Color.BLUE
        set(value) {
            field = value
            postInvalidate()
        }
    @ColorInt
    var subLineColor = Color.GRAY
        set(value) {
            field = value
            postInvalidate()
        }
    @ColorInt
    @Size(2) private var shadowColors = arrayOf(Color.BLACK, Color.WHITE).toIntArray()
        set(value) {
            field = value
            postInvalidate()
        }

    // inner data.

    var polylineKeyPoints = emptyArray<PolylineKeyPoint>()

    val polylineWidth = DisplayUtils.dpToPx(context, POLYLINE_SIZE_DIP)

    private val polylinePaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = polylineWidth
        isAntiAlias = true
        isFilterBitmap = true
        style = Paint.Style.STROKE
    }
    private val shadowPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    private val polylinePath = Path()
    private val shadowPath = Path()
    private val shaderWrapper = DayNightShaderWrapper(width, height)

    // measure.

    private fun getLayoutX(xPercent: Float) = if (DisplayUtils.isRtl(context)) {
        (measuredHeight - paddingRight) - (measuredWidth - (paddingLeft + paddingRight)) * xPercent
    } else {
        paddingLeft + (measuredWidth - (paddingLeft + paddingRight)) * xPercent
    }

    private fun getLayoutY(yPercent: Float) =
        (measuredHeight - paddingBottom) - (measuredHeight - (paddingTop + paddingBottom)) * yPercent

    override fun getPaddingLeft() = (parent as PrecipitationBar).paddingLeft
    override fun getPaddingTop() = (parent as PrecipitationBar).paddingTop
    override fun getPaddingRight() = (parent as PrecipitationBar).paddingRight
    override fun getPaddingBottom() = (parent as PrecipitationBar).paddingBottom
    override fun getPaddingStart() = (parent as PrecipitationBar).paddingStart
    override fun getPaddingEnd() = (parent as PrecipitationBar).paddingEnd

    // measure.

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        polylineKeyPoints = values.mapIndexed { index, value ->
            val xPercent = index.toFloat() / (values.size - 1).toFloat()
            val yPercent = value.toFloat()

            val x = getLayoutX(xPercent)
            val y = getLayoutY(yPercent)
            PolylineKeyPoint(originCenterX = x, originTopY = y)
        }.toTypedArray()
    }

    // draw.

    fun setShadowColors(@ColorInt colorHigh: Int, @ColorInt colorLow: Int, lightTheme: Boolean) {
        shadowColors[0] = if (lightTheme) {
            ColorUtils.setAlphaComponent(
                colorHigh,
                (255 * SHADOW_ALPHA_FACTOR_LIGHT).toInt()
            )
        } else {
            ColorUtils.setAlphaComponent(
                colorLow,
                (255 * SHADOW_ALPHA_FACTOR_DARK).toInt()
            )
        }
        shadowColors[1] = Color.TRANSPARENT
        ensureShader(lightTheme)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        polylinePaint.color = precipitationColor

        ensureShader(shaderWrapper.isLightTheme)
        shadowPaint.shader = shaderWrapper.shader

        if (polylineKeyPoints.size >= 2) {
            polylinePath.reset()
            shadowPath.reset()

            polylinePath.moveTo(polylineKeyPoints[0].originCenterX, polylineKeyPoints[0].originTopY)
            shadowPath.moveTo(polylineKeyPoints[0].originCenterX, polylineKeyPoints[0].originTopY)

            polylineKeyPoints.slice(1 until polylineKeyPoints.size).forEach {
                polylinePath.lineTo(it.originCenterX, it.originTopY)
                shadowPath.lineTo(it.originCenterX, it.originTopY)
            }

            shadowPath.lineTo(polylineKeyPoints.last().originCenterX, measuredHeight.toFloat())
            shadowPath.lineTo(polylineKeyPoints.first().originCenterX, measuredHeight.toFloat())
            shadowPath.close()

            canvas.drawPath(shadowPath, shadowPaint)
            canvas.drawPath(polylinePath, polylinePaint)
        }
    }

    private fun ensureShader(lightTheme: Boolean) {
        if (shaderWrapper.isDifferent(measuredWidth, measuredHeight, lightTheme, shadowColors)) {
            shaderWrapper.setShader(
                LinearGradient(
                    0F, paddingTop.toFloat(),
                    0F, measuredHeight.toFloat(),
                    shadowColors[0], shadowColors[1],
                    Shader.TileMode.CLAMP
                ),
                measuredWidth, measuredHeight,
                lightTheme,
                shadowColors
            )
        }
    }
}
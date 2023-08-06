/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.ui.widgets.precipitationBar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.google.android.material.button.MaterialButton
import org.breezyweather.common.extensions.dpToPx
import kotlin.math.abs

private const val INDICATOR_TEXT_SIZE_DIP = 12f
private const val INDICATOR_HEIGHT = 36f
private const val INDICATOR_MARGIN = 2f
private const val TOUCH_LINE_WIDTH = 2f

private const val TAG = "PrecipitationBar"

class PrecipitationBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // public data.

    var precipitationIntensities = emptyArray<Double>()
        set(value) {
            field = value

            val maxIntensity = value.maxOrNull() ?: 0.0
            backgroundView.values = value.map {
                minOf(1.0, it / maxIntensity)
            }.toTypedArray()

            highlightIndex = maxOf(
                0,
                value.indexOfFirst { it == maxIntensity }
            )
            requestLayout()
        }
    private var highlightIndex = 0

    var precipitationColor: Int
        get() = backgroundView.precipitationColor
        set(value) {
            backgroundView.precipitationColor = value
        }

    var subLineColor: Int
        get() = backgroundView.subLineColor
        set(value) {
            backgroundView.subLineColor = value
        }

    @ColorInt var highlightColor = Color.CYAN
        set(value) {
            field = value
            indicator.backgroundTintList = ColorStateList.valueOf(value)
            touchLine.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                arrayOf(value, Color.TRANSPARENT).toIntArray()
            )
        }

    @ColorInt var textColor = Color.WHITE
        set(value) {
            field = value
            indicator.setTextColor(ColorStateList.valueOf(value))
        }

    interface IndicatorGenerator {
        fun getIndicatorContent(precipitation: Double): String
    }
    var indicatorGenerator: IndicatorGenerator? = null

    // subviews.

    private val touchLine = View(context).apply {
        background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            arrayOf(highlightColor, Color.TRANSPARENT).toIntArray()
        )
    }.also { addView(it) }
    private val backgroundView = PrecipitationBarBackgroundView(context).also { addView(it) }
    private val indicator = MaterialButton(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, context.dpToPx(INDICATOR_TEXT_SIZE_DIP))
        backgroundTintList = ColorStateList.valueOf(highlightColor)
        alpha = 0F
    }.also { addView(it) }

    // inner data.

    private val indicatorHeight = context.dpToPx(INDICATOR_HEIGHT)
    private val indicatorMargin = context.dpToPx(INDICATOR_MARGIN)
    private val touchLineWidth = context.dpToPx(TOUCH_LINE_WIDTH)

    private var pointerId = -1
    private var initialX = 0F
    private var initialY = 0F
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var beingDragged = false
    private var horizontalDragged = false

    // init.

    init {
        clipToPadding = false
        clipToOutline = false
    }

    // measure.

    override fun getPaddingLeft() = super.getPaddingLeft() + backgroundView.polylineWidth.toInt()
    override fun getPaddingTop() = super.getPaddingTop() + backgroundView.polylineWidth.toInt()
    override fun getPaddingRight() = super.getPaddingRight() + backgroundView.polylineWidth.toInt()
    override fun getPaddingBottom() = super.getPaddingBottom() + backgroundView.polylineWidth.toInt()
    override fun getPaddingStart() = super.getPaddingStart() + backgroundView.polylineWidth.toInt()
    override fun getPaddingEnd() = super.getPaddingEnd() + backgroundView.polylineWidth.toInt()

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        backgroundView.measure(
            MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        )

        indicator.measure(
            MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(indicatorHeight.toInt(), MeasureSpec.EXACTLY)
        )

        touchLine.measure(
            MeasureSpec.makeMeasureSpec(touchLineWidth.toInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(
                (measuredHeight
                        - paddingTop
                        + backgroundView.polylineWidth / 2F
                        + indicatorMargin
                        + indicator.measuredHeight / 2F).toInt(),
                MeasureSpec.EXACTLY
            )
        )
    }

    // layout.

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        val indicatorTop = (paddingTop
                - backgroundView.polylineWidth / 2.0F
                - indicatorMargin
                - indicator.measuredHeight).toInt()
        indicator.layout(0, indicatorTop, indicator.measuredWidth, indicatorTop + indicator.measuredHeight)
        indicatorGenerator?.getIndicatorContent(precipitationIntensities[highlightIndex]).let {
            if (indicator.text != it) {
                indicator.text = it
            }
        }
        if (indicator.alpha != 1F) {
            indicator.alpha = 1F
        }

        val touchLineTop = (indicatorTop + indicator.measuredHeight / 2.0).toInt()
        touchLine.layout(0, touchLineTop, touchLine.measuredWidth, touchLineTop + touchLine.measuredHeight)

        backgroundView.layout(0, 0, backgroundView.measuredWidth, backgroundView.measuredHeight)

        getTranslationXOfIndicator(highlightIndex).let {
            if (indicator.translationX != it) {
                indicator.translationX = it
            }
        }
        getTranslationXOfTouchLine(highlightIndex).let {
            if (touchLine.translationX != it) {
                touchLine.translationX = it
            }
        }
    }

    // draw.

    fun setShadowColors(@ColorInt colorHigh: Int, @ColorInt colorLow: Int, lightTheme: Boolean) {
        backgroundView.setShadowColors(colorHigh, colorLow, lightTheme)
    }

    // touch.

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        super.onInterceptTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                beingDragged = false
                horizontalDragged = false
                pointerId = ev.getPointerId(0)
                initialX = ev.x
                initialY = ev.y
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                pointerId = ev.getPointerId(index)
                initialX = ev.getX(index)
                initialY = ev.getY(index)
            }
            MotionEvent.ACTION_MOVE -> {
                val index = ev.findPointerIndex(pointerId)
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=$pointerId in onTouchEvent")
                } else {
                    val x = ev.getX(index)
                    val y = ev.getY(index)
                    if (!beingDragged && !horizontalDragged) {
                        if (abs(x - initialX) > touchSlop || abs(y - initialY) > touchSlop) {
                            beingDragged = true
                            if (abs(x - initialX) > abs(y - initialY)) {
                                horizontalDragged = true
                            } else {
                                parent.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = ev.actionIndex
                val id = ev.getPointerId(index)
                if (pointerId == id) {
                    val newIndex = if (index == 0) 1 else 0
                    this.pointerId = ev.getPointerId(newIndex)
                    initialX = ev.getX(newIndex).toInt().toFloat()
                    initialY = ev.getY(newIndex).toInt().toFloat()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                beingDragged = false
                horizontalDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return beingDragged && horizontalDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                pointerId = ev.getPointerId(index)
                initialX = ev.getX(index)
                initialY = ev.getY(index)
            }
            MotionEvent.ACTION_MOVE -> {
                val index = ev.findPointerIndex(pointerId)
                if (index == -1) {
                    Log.e(TAG, "Invalid pointerId=$pointerId in onTouchEvent")
                } else {
                    val x = ev.getX(index)
                    val y = ev.getY(index)
                    if (!beingDragged && !horizontalDragged) {
                        beingDragged = true
                        if (abs(x - initialX) > abs(y - initialY)) {
                            horizontalDragged = true
                        } else {
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = ev.actionIndex
                val id = ev.getPointerId(index)
                if (pointerId == id) {
                    val newIndex = if (index == 0) 1 else 0
                    this.pointerId = ev.getPointerId(newIndex)
                    initialX = ev.getX(newIndex).toInt().toFloat()
                    initialY = ev.getY(newIndex).toInt().toFloat()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                beingDragged = false
                horizontalDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        indexPolylineKeyPoint(ev.x)?.let {
            if (highlightIndex != it) {
                highlightIndex = it
                requestLayout()
            }
        }
        return true
    }

    private fun indexPolylineKeyPoint(x: Float): Int? {
        val keyPointWidth = (
                measuredWidth - paddingLeft - paddingRight
        ).toFloat() / (
                backgroundView.polylineKeyPoints.size - 1
        )

        for ((index, keyPoint) in backgroundView.polylineKeyPoints.withIndex()) {
            val deltaX = abs(x - keyPoint.originCenterX)
            if (deltaX < 0.5F * keyPointWidth || deltaX == 0.5F * keyPointWidth) {
                return index
            }
        }
        return null
    }

    private fun getTranslationXOfIndicator(index: Int): Float {
        val keyPoint = backgroundView.polylineKeyPoints[index]

        var tx = keyPoint.originCenterX - indicator.measuredWidth / 2.0F

        val minBounceOriginX = paddingLeft
        val maxBounceOriginX = measuredWidth - paddingRight - indicator.measuredWidth

        if (tx < minBounceOriginX) {
            tx = minBounceOriginX - 0.3F * (minBounceOriginX - tx)
        } else if (maxBounceOriginX < tx) {
            tx = maxBounceOriginX + 0.3F * (tx - maxBounceOriginX)
        }

        return tx - indicator.left
    }

    private fun getTranslationXOfTouchLine(index: Int): Float {
        val keyPoint = backgroundView.polylineKeyPoints[index]
        return keyPoint.originCenterX - touchLine.measuredWidth / 2.0F - touchLine.left
    }
}
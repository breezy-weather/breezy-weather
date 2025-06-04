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

package org.breezyweather.ui.theme.weatherView.materialWeatherView

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.Size
import org.breezyweather.ui.theme.weatherView.WeatherView
import org.breezyweather.ui.theme.weatherView.WeatherView.WeatherKindRule
import kotlin.math.min

class MaterialWeatherView(
    context: Context,
) : ViewGroup(context), WeatherView {
    private var mCurrentView: MaterialPainterView? = null
    private var mPreviousView: MaterialPainterView? = null
    private var mSwitchAnimator: Animator? = null

    @WeatherKindRule
    override var weatherKind = 0
        private set
    private var mDaytime = false
    private var mFirstCardMarginTop = 0
    private var mGravitySensorEnabled: Boolean = true
    private var mAnimate: Boolean = true
    private var mDrawable: Boolean = false

    /**
     * This class is used to implement different kinds of weather animations.
     */
    abstract class WeatherAnimationImplementor {
        abstract fun updateData(
            @Size(2) canvasSizes: IntArray,
            interval: Long,
            rotation2D: Float,
            rotation3D: Float,
        )

        // return true if finish drawing.
        abstract fun draw(
            @Size(2) canvasSizes: IntArray,
            canvas: Canvas,
            scrollRate: Float,
            rotation2D: Float,
            rotation3D: Float,
        )
    }

    abstract class RotateController {
        abstract fun updateRotation(rotation: Double, interval: Double)
        abstract val rotation: Double
    }

    init {
        setWeather(WeatherView.WEATHER_KIND_NULL, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // At what position will animations disappear
        // TODO: Stops too early, should take into account "current details" height
        mFirstCardMarginTop = (resources.displayMetrics.heightPixels * 0.25).toInt() // 0.66
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            child.measure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            child.layout(
                0,
                0,
                child.measuredWidth,
                child.measuredHeight
            )
        }
    }

    // interface.
    // weather view.
    override fun setWeather(
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
    ) {
        // do nothing if weather not change.
        if (this.weatherKind == weatherKind && mDaytime == daytime) {
            return
        }

        // cache weather state.
        this.weatherKind = weatherKind
        mDaytime = daytime

        // cancel the previous switch animation if necessary.
        mSwitchAnimator?.let {
            it.cancel()
            mSwitchAnimator = null
        }

        // stop current painting work.
        mCurrentView?.let {
            it.drawable = false
        }

        // generate new painter view or update painter cache.
        val prev = mPreviousView
        mPreviousView = mCurrentView
        mCurrentView = prev
        mCurrentView?.let {
            it.update(weatherKind, daytime, mGravitySensorEnabled, mAnimate)
            it.drawable = mDrawable
        } ?: run {
            mCurrentView = MaterialPainterView(
                context,
                weatherKind,
                daytime,
                mDrawable,
                mPreviousView?.scrollRate ?: 0f,
                mGravitySensorEnabled,
                mAnimate
            )
            addView(mCurrentView)
        }

        // execute switch animation.
        mPreviousView?.let {
            mSwitchAnimator = AnimatorSet().apply {
                duration = SWITCH_ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
                playTogether(
                    ObjectAnimator.ofFloat(mCurrentView as MaterialPainterView, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(
                        mPreviousView as MaterialPainterView,
                        "alpha",
                        it.alpha,
                        0f
                    )
                )
            }.also { it.start() }
        } ?: run {
            mCurrentView?.alpha = 1f
        }
    }

    override fun onScroll(scrollY: Int) {
        val scrollRate = min(1.0, 1.0 * scrollY / mFirstCardMarginTop).toFloat()
        mCurrentView?.let {
            it.scrollRate = scrollRate
        }
        mPreviousView?.let {
            it.scrollRate = scrollRate
        }
    }

    override fun setDrawable(drawable: Boolean) {
        if (mDrawable == drawable) {
            return
        }
        mDrawable = drawable
        mCurrentView?.let {
            it.drawable = drawable
        }
        mPreviousView?.let {
            it.drawable = drawable
        }
    }

    override fun setDoAnimate(animate: Boolean) {
        mAnimate = animate
    }

    override fun setGravitySensorEnabled(enabled: Boolean) {
        mGravitySensorEnabled = enabled
    }

    companion object {
        private const val SWITCH_ANIMATION_DURATION: Long = 300
    }
}

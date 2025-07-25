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

package org.breezyweather.ui.common.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.core.view.isNotEmpty
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getTabletListAdaptiveWidth
import org.breezyweather.common.extensions.isLandscape
import org.breezyweather.common.extensions.isRtl
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {
    private var mDrawer: View? = null
    private var mContent: View? = null
    private var mUnfold: Boolean

    // 0.0 - fold, 1.0 - unfold
    @FloatRange(from = 0.0, to = 1.0)
    private var mProgress: Float
    private var mProgressAnimator: ValueAnimator? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DrawerLayout, defStyleAttr, 0)
        mUnfold = a.getBoolean(R.styleable.DrawerLayout_unfold, false) && context.isLandscape
        mProgress = if (mUnfold) 1f else 0f
        a.recycle()
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isNotEmpty()) {
            mDrawer = getChildAt(0)
        }
        if (childCount > 1) {
            mContent = getChildAt(1)
        }
        var lp: LayoutParams
        mDrawer?.let { drawer ->
            lp = drawer.layoutParams
            var width = lp.width
            if (width == LayoutParams.WRAP_CONTENT) {
                width = measuredWidth - context.getTabletListAdaptiveWidth(measuredWidth)
                if (width == 0) {
                    width = LayoutParams.MATCH_PARENT
                } else {
                    val minDrawerWidth = context.dpToPx(MIN_DRAWER_WIDTH_DP.toFloat()).toInt()
                    val maxDrawerWidth = context.dpToPx(MAX_DRAWER_WIDTH_DP.toFloat()).toInt()
                    width = max(width, minDrawerWidth)
                    width = min(width, maxDrawerWidth)
                }
            }
            drawer.measure(
                getChildMeasureSpec(widthMeasureSpec, 0, width),
                getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
            )
            mContent?.let { content ->
                lp = content.layoutParams
                if (drawer.measuredWidth == measuredWidth) {
                    content.measure(
                        getChildMeasureSpec(widthMeasureSpec, 0, lp.width),
                        getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
                    )
                } else {
                    val widthUsed = (drawer.measuredWidth * mProgress).toInt()
                    content.measure(
                        getChildMeasureSpec(widthMeasureSpec, widthUsed, lp.width),
                        getChildMeasureSpec(heightMeasureSpec, 0, lp.height)
                    )
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isNotEmpty()) {
            mDrawer = getChildAt(0)
        }
        if (childCount > 1) {
            mContent = getChildAt(1)
        }
        if (context.isRtl) {
            mDrawer?.let { drawer ->
                drawer.layout(
                    (measuredWidth - drawer.measuredWidth * mProgress).toInt(),
                    0,
                    (measuredWidth + drawer.measuredWidth * (1 - mProgress)).toInt(),
                    drawer.measuredHeight
                )
                mContent?.let { content ->
                    content.layout(
                        0,
                        0,
                        drawer.left,
                        content.measuredHeight
                    )
                }
            }
        } else {
            mDrawer?.let { drawer ->
                drawer.layout(
                    (drawer.measuredWidth * (mProgress - 1)).toInt(),
                    0,
                    (drawer.measuredWidth * mProgress).toInt(),
                    drawer.measuredHeight
                )
                mContent?.let { content ->
                    content.layout(
                        drawer.right,
                        0,
                        drawer.right + content.measuredWidth,
                        content.measuredHeight
                    )
                }
            }
        }
    }

    var isUnfold: Boolean
        get() = mUnfold
        set(unfold) {
            if (mUnfold == unfold) {
                return
            }
            mUnfold = unfold
            mProgressAnimator?.let {
                it.cancel()
                mProgressAnimator = null
            }
            mProgressAnimator = generateProgressAnimator(mProgress, if (unfold) 1f else 0f).also { it.start() }
        }

    private fun generateProgressAnimator(from: Float, to: Float): ValueAnimator {
        return ValueAnimator.ofFloat(from, to).apply {
            addUpdateListener { animation: ValueAnimator -> setProgress(animation.animatedValue as Float) }
            duration = (abs(from - to) * 450).toLong()
            interpolator = DecelerateInterpolator(2f)
        }
    }

    private fun setProgress(progress: Float) {
        mProgress = progress
        requestLayout()
    }

    companion object {
        private const val MIN_DRAWER_WIDTH_DP = 280
        private const val MAX_DRAWER_WIDTH_DP = 320
    }
}

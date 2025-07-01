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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.core.view.isNotEmpty

class SwipeSwitchLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent2, NestedScrollingParent3 {
    private var mTarget: View? = null
    private var mResetAnimation: SpringAnimation? = null
    private var mSwitchListener: OnSwitchListener? = null
    private var mPageSwipeListener: OnPagerSwipeListener? = null
    var totalCount = 1
        private set
    private var mPosition = 0
    private var mSwipeDistance = 0
    private var mSwipeTrigger = 500
    private var mNestedScrollingDistance = 0f
    private var mNestedScrollingTrigger = 300f
    private var mLastX = 0f
    private var mLastY = 0f
    private val mTouchSlop: Int
    private var mIsBeingTouched = false
    private var mIsBeingDragged = false
    private var mIsHorizontalDragged = false
    private var mIsBeingNestedScrolling = false

    interface OnSwitchListener {
        fun onSwiped(swipeDirection: Int, progress: Float)
        fun onSwitched(swipeDirection: Int)
    }

    interface OnPagerSwipeListener {
        fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
        fun onPageSelected(position: Int)
    }

    init {
        mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
    }

    // layout.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mSwipeTrigger = (measuredWidth * SWIPE_DISTANCE_RATIO).roundToInt()
        mNestedScrollingTrigger = mSwipeTrigger.toFloat()
    }

    // touch.
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled || ev.action != MotionEvent.ACTION_DOWN && mIsBeingNestedScrolling) {
            return false
        }
        if (mTarget == null && isNotEmpty()) {
            mTarget = getChildAt(0)
        }
        if (mTarget == null) return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                cancelResetAnimation()
                mIsBeingTouched = true
                mIsBeingDragged = false
                mIsHorizontalDragged = false
                mLastX = ev.x
                mLastY = ev.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingTouched) {
                    mIsBeingTouched = true
                    mLastX = ev.x
                    mLastY = ev.y
                }
                val x = ev.x
                val y = ev.y
                if (!mIsBeingDragged && !mIsHorizontalDragged) {
                    if (abs(x - mLastX) > mTouchSlop || abs(y - mLastY) > mTouchSlop) {
                        mIsBeingDragged = true
                        if (abs(x - mLastX) > abs(y - mLastY)) {
                            mLastX += (if (x > mLastX) mTouchSlop else -mTouchSlop).toFloat()
                            mIsHorizontalDragged = true
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingTouched = false
                mIsBeingDragged = false
                mIsHorizontalDragged = false
            }
        }
        return mIsBeingDragged && mIsHorizontalDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled || mIsBeingNestedScrolling) return false
        if (mTarget == null && isNotEmpty()) {
            mTarget = getChildAt(0)
        }
        if (mTarget == null) return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                cancelResetAnimation()
                mIsBeingTouched = true
                mIsBeingDragged = false
                mIsHorizontalDragged = false
                mLastX = ev.x
                mLastY = ev.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (mIsBeingDragged && mIsHorizontalDragged) {
                    mSwipeDistance += (ev.x - mLastX).toInt()
                    setTranslation(mSwipeTrigger, SWIPE_RATIO)
                }
                mLastX = ev.x
                mLastY = ev.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingTouched = false
                release(mSwipeTrigger)
            }
        }
        return true
    }

    // control.
    fun reset() {
        cancelResetAnimation()
        mIsBeingDragged = false
        mIsHorizontalDragged = false
        mSwipeDistance = 0
        mNestedScrollingDistance = 0f
        setTranslation(mSwipeTrigger, SWIPE_RATIO)
    }

    private fun cancelResetAnimation() {
        mResetAnimation?.let {
            it.cancel()
            mResetAnimation = null
        }
    }

    private fun setTranslation(triggerDistance: Int, translateRatio: Float) {
        val realDistance = mSwipeDistance
            .coerceAtMost(triggerDistance)
            .coerceAtLeast(-triggerDistance)
            .toFloat()
        val swipeDirection = if (mSwipeDistance < 0) SWIPE_DIRECTION_LEFT else SWIPE_DIRECTION_RIGHT
        val progress = abs(realDistance) / triggerDistance
        mTarget?.alpha = 1 - progress
        mTarget?.translationX = (
            swipeDirection * translateRatio * triggerDistance
                * log10(1 + 9.0 * abs(mSwipeDistance) / triggerDistance)
            ).toFloat()
        mSwitchListener?.onSwiped(swipeDirection, progress)
        mPageSwipeListener?.let {
            if (mSwipeDistance > 0) {
                it.onPageScrolled(
                    mPosition - 1,
                    1 - min(1f, 1f * mSwipeDistance / triggerDistance),
                    max(0, triggerDistance - mSwipeDistance)
                )
            } else {
                it.onPageScrolled(
                    mPosition,
                    min(1f, -1f * mSwipeDistance / triggerDistance),
                    min(-mSwipeDistance, triggerDistance)
                )
            }
        }
    }

    private fun release(triggerDistance: Int) {
        val swipeDirection = if (mSwipeDistance < 0) SWIPE_DIRECTION_LEFT else SWIPE_DIRECTION_RIGHT
        if (abs(mSwipeDistance) > abs(triggerDistance)) {
            position = swipeDirection
            mSwitchListener?.onSwitched(swipeDirection)
            mPageSwipeListener?.onPageSelected(mPosition)
        } else {
            if (mTarget == null) {
                reset()
                return
            }
            mResetAnimation = SpringAnimation(FloatValueHolder(mSwipeDistance.toFloat())).apply {
                spring = SpringForce(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                addUpdateListener { _: DynamicAnimation<*>?, value: Float, _: Float ->
                    mSwipeDistance = value.toInt()
                    setTranslation(mSwipeTrigger, SWIPE_RATIO)
                }
            }.also { it.start() }
        }
    }

    // interface.
    fun setData(currentIndex: Int, pageCount: Int) {
        if (currentIndex < 0 || currentIndex >= pageCount) {
            // Ignore, happens when location list is empty (initial install)
            // throw RuntimeException("Invalid current index.")
        } else {
            mPosition = currentIndex
            totalCount = pageCount
        }
    }

    var position: Int
        get() = mPosition
        private set(swipeDirection) {
            when (swipeDirection) {
                SWIPE_DIRECTION_LEFT -> mPosition++
                SWIPE_DIRECTION_RIGHT -> mPosition--
            }
            if (mPosition < 0) {
                mPosition = totalCount - 1
            } else if (mPosition > totalCount - 1) {
                mPosition = 0
            }
        }

    // interface.
    fun setOnSwitchListener(l: OnSwitchListener?) {
        mSwitchListener = l
    }

    fun setOnPageSwipeListener(l: OnPagerSwipeListener?) {
        mPageSwipeListener = l
    }

    // nested scrolling parent.
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if (mTarget == null && isNotEmpty()) {
            mTarget = getChildAt(0)
        }
        return axes and ViewCompat.SCROLL_AXIS_HORIZONTAL != 0 &&
            mSwitchListener != null &&
            type == ViewCompat.TYPE_TOUCH &&
            isEnabled &&
            mTarget != null
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        if (!mIsBeingNestedScrolling) {
            mIsBeingNestedScrolling = true
            mNestedScrollingDistance =
                if (!target.canScrollHorizontally(-1) &&
                    !target.canScrollHorizontally(1) ||
                    mSwipeDistance != 0
                ) {
                    mNestedScrollingTrigger
                } else {
                    0f
                }
        }
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        mIsBeingNestedScrolling = false
        release(mSwipeTrigger)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (mSwipeDistance != 0) {
            if (mSwipeDistance > 0 && mSwipeDistance - dx < 0 || mSwipeDistance < 0 && mSwipeDistance - dx > 0) {
                consumed[0] = mSwipeDistance
            } else {
                consumed[0] = dx
            }
            innerNestedScroll(consumed[0])
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        innerNestedScroll(dxUnconsumed)
        consumed[0] += dxUnconsumed
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
    ) {
        innerNestedScroll(dxUnconsumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return false
    }

    private fun innerNestedScroll(dxUnconsumed: Int) {
        if (abs(mNestedScrollingDistance) >= mNestedScrollingTrigger) {
            mSwipeDistance -= dxUnconsumed
        } else {
            mNestedScrollingDistance -= dxUnconsumed.toFloat()
            mSwipeDistance = (mSwipeDistance - dxUnconsumed / 10f).toInt()
            if (abs(mNestedScrollingDistance) >= mNestedScrollingTrigger) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
        setTranslation(mSwipeTrigger, NESTED_SCROLLING_RATIO)
    }

    companion object {
        private const val SWIPE_DISTANCE_RATIO = 0.5
        private const val SWIPE_RATIO = 0.4f
        private const val NESTED_SCROLLING_RATIO = SWIPE_RATIO // 0.075f
        const val SWIPE_DIRECTION_LEFT = -1
        const val SWIPE_DIRECTION_RIGHT = 1
    }
}

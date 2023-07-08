package org.breezyweather.main.adapters

import android.animation.*
import android.content.Context
import android.view.View
import org.breezyweather.R
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.ui.adapters.AnimationAdapterWrapper
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.main.adapters.location.LocationAdapter
import org.breezyweather.main.adapters.location.LocationHolder
import kotlin.math.max
import kotlin.math.min

class LocationAdapterAnimWrapper(
    context: Context, adapter: LocationAdapter
) : AnimationAdapterWrapper<LocationAdapter, LocationHolder>(adapter, true) {
    private var mStartAnimation = false
    private var mScrolled = false
    private val mDY: Float
    private val mDZ: Float

    init {
        mDY = context.dpToPx(256f)
        mDZ = context.dpToPx(10f)
    }

    override fun getAnimator(view: View, pendingCount: Int): Animator? {
        if (pendingCount == 0) {
            mStartAnimation = if (mStartAnimation) {
                setLastPosition(Int.MAX_VALUE)
                return null
            } else {
                true
            }
        }
        val duration = max(
            BASE_DURATION - pendingCount * 50,
            BASE_DURATION - MAX_TENSOR_COUNT * 50
        )
        val delay = (if (mScrolled) 50 else pendingCount * 100).toLong()
        val overShootTensor = 0.2f + min(
            pendingCount * 0.4f,
            MAX_TENSOR_COUNT * 0.4f
        )
        val alpha: Animator = ObjectAnimator
            .ofFloat(view, "alpha", 0f, 1f).setDuration(duration / 4 * 3)
        alpha.interpolator = DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR
        val animators = DisplayUtils.getFloatingOvershotEnterAnimators(
            view, overShootTensor,
            mDY, 1.1f, 1.1f
        )
        for (a in animators) {
            a.setDuration(duration)
        }
        val z: Animator = ObjectAnimator.ofFloat(view, "translationZ", mDZ, 0f).setDuration(duration)
        z.interpolator = DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR
        return AnimatorSet().apply {
            playTogether(alpha, animators[0], animators[1], animators[2], z)
            startDelay = delay
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    setItemStateListAnimator(view, true)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setItemStateListAnimator(view, true)
                }
            })
        }
    }

    override fun setInitState(view: View) {
        view.apply {
            alpha = 0f
            translationY = mDY
            scaleX = SCALE_FROM
            scaleY = SCALE_FROM
        }
        setItemStateListAnimator(view, false)
    }

    private fun setItemStateListAnimator(view: View, enabled: Boolean) {
        view.stateListAnimator = if (enabled) AnimatorInflater.loadStateListAnimator(
            view.context,
            R.animator.touch_raise
        ) else null
    }

    fun setScrolled() {
        mScrolled = true
    }

    companion object {
        private const val SCALE_FROM = 1.1f
        private const val BASE_DURATION: Long = 600
        private const val MAX_TENSOR_COUNT = 6
    }
}

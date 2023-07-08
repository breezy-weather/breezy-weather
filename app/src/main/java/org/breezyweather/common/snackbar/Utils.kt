package org.breezyweather.common.snackbar

import android.animation.Animator
import android.animation.AnimatorSet
import android.graphics.Rect
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.breezyweather.common.basic.insets.FitBothSideBarHelper
import org.breezyweather.common.basic.insets.FitBothSideBarView
import org.breezyweather.common.utils.DisplayUtils

object Utils : AnimationUtils() {
    val FAST_OUT_SLOW_IN_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()

    fun getEnterAnimator(view: View, cardStyle: Boolean): Animator {
        view.translationY = view.height.toFloat()
        view.scaleX = if (cardStyle) 1.1f else 1f
        view.scaleY = if (cardStyle) 1.1f else 1f
        val animators = DisplayUtils.getFloatingOvershotEnterAnimators(view)
        if (!cardStyle) {
            animators[0].interpolator = DisplayUtils.FLOATING_DECELERATE_INTERPOLATOR
        }
        return AnimatorSet().apply {
            playTogether(animators[0], animators[1], animators[2])
        }
    }

    fun consumeInsets(view: View, insets: Rect) {
        val fitInsetsHelper = FitBothSideBarHelper(
            view, FitBothSideBarView.SIDE_BOTTOM
        )
        fitInsetsHelper.fitSystemWindows(insets) {
            insets.set(fitInsetsHelper.windowInsets)
            view.requestLayout()
        }
    }
}
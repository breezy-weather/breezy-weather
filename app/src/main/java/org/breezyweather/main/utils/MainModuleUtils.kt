package org.breezyweather.main.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import org.breezyweather.common.utils.DisplayUtils
import kotlin.math.max

object MainModuleUtils {

    private const val BASE_ENTER_DURATION: Long = 500

    @JvmStatic
    fun getEnterAnimator(view: View, pendingCount: Int): Animator {
        val animators = DisplayUtils.getFloatingOvershotEnterAnimators(
            view, 0.4f + 0.2f * pendingCount,
            DisplayUtils.dpToPx(view.context, 120f), 1.025f, 1.025f
        )
        return AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                animators[0],
                animators[1],
                animators[2]
            )
            duration = max(BASE_ENTER_DURATION - pendingCount * 50L, BASE_ENTER_DURATION / 2)
            startDelay = pendingCount * 200L
        }
    }
}

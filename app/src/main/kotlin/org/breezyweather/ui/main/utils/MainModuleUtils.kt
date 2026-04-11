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

package org.breezyweather.ui.main.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.common.extensions.getFloatingOvershotEnterAnimators
import kotlin.math.max

object MainModuleUtils {

    private const val BASE_ENTER_DURATION: Long = 500

    fun getEnterAnimator(view: View, pendingCount: Int): Animator {
        val animators = view.getFloatingOvershotEnterAnimators(
            0.4f + 0.2f * pendingCount,
            view.context.dpToPx(120f),
            1.025f,
            1.025f
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

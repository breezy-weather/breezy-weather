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

package org.breezyweather.common.ui.transitions

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import org.breezyweather.R

class ScaleTransition(context: Context, attrs: AttributeSet?) : Transition(context, attrs) {
    private val mShow: Boolean

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ScaleTransition)
        mShow = a.getInt(R.styleable.ScaleTransition_scale_type, TYPE_SHOW) == TYPE_SHOW
        a.recycle()
    }

    override fun getTransitionProperties(): Array<String> {
        return Companion.transitionProperties
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_X] = if (mShow) 0f else 1f
        transitionValues.values[PROPNAME_Y] = if (mShow) 0f else 1f
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        transitionValues.values[PROPNAME_X] = if (mShow) transitionValues.view.scaleX else 0f
        transitionValues.values[PROPNAME_Y] =
            if (mShow) transitionValues.view.scaleY else 0f
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) return null
        if (mShow) {
            endValues.view.scaleX = 0f
            endValues.view.scaleY = 0f
        }
        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(
                endValues.view,
                "scaleX",
                (startValues.values[PROPNAME_X] as Float?)!!,
                (endValues.values[PROPNAME_X] as Float?)!!
            ),
            ObjectAnimator.ofFloat(
                endValues.view,
                "scaleY",
                (startValues.values[PROPNAME_Y] as Float?)!!,
                (endValues.values[PROPNAME_Y] as Float?)!!
            )
        )
        return set
    }

    companion object {
        private const val TYPE_SHOW = 1
        private const val TYPE_HIDE = 2
        private const val PROPNAME_X = "breezyweather:scale:x"
        private const val PROPNAME_Y = "breezyweather:scale:y"
        private val transitionProperties = arrayOf(
            PROPNAME_X, PROPNAME_Y
        )
    }
}
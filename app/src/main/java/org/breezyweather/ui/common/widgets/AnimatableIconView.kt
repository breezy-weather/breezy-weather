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

import android.animation.Animator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Size
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import org.breezyweather.R

class AnimatableIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    @Size(3)
    private var mIconImageViews: Array<AppCompatImageView>

    @Size(3)
    private val mIconAnimators: Array<Animator?> = arrayOf(null, null, null)

    init {
        val attributes = context.theme
            .obtainStyledAttributes(attrs, R.styleable.AnimatableIconView, defStyleAttr, 0)
        val innerMargin = attributes.getDimensionPixelSize(R.styleable.AnimatableIconView_inner_margins, 0)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.setMargins(innerMargin, innerMargin, innerMargin, innerMargin)
        mIconImageViews = arrayOf(
            AppCompatImageView(context),
            AppCompatImageView(context),
            AppCompatImageView(context)
        )
        for (i in mIconImageViews.indices.reversed()) {
            addView(mIconImageViews[i], params)
        }
        attributes.recycle()
    }

    fun setAnimatableIcon(
        @Size(3) drawables: Array<Drawable?>,
        @Size(3) animators: Array<Animator?>,
    ) {
        endAnimators()
        for (i in drawables.indices) {
            mIconImageViews[i].setImageDrawable(drawables[i])
            mIconImageViews[i].visibility = if (drawables[i] == null) GONE else VISIBLE
            mIconAnimators[i] = animators[i]
            mIconAnimators[i]?.setTarget(mIconImageViews[i])
        }
    }

    fun startAnimators() {
        for (a in mIconAnimators) {
            if (a != null && a.isStarted) {
                // animating.
                return
            }
        }
        for (i in mIconAnimators.indices) {
            if (mIconImageViews[i].isVisible) {
                mIconAnimators[i]?.start()
            }
        }
    }

    private fun endAnimators() {
        for (i in mIconImageViews.indices) {
            mIconAnimators[i]?.let {
                if (it.isStarted) {
                    mIconAnimators[i]?.cancel()
                }
            }
            resetView(mIconImageViews[i])
        }
    }

    private fun resetView(view: View) {
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        view.rotation = 0f
        view.rotationX = 0f
        view.rotationY = 0f
        view.translationX = 0f
        view.translationY = 0f
    }
}

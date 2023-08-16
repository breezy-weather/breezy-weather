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

package org.breezyweather.common.basic.insets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat

class FitHorizontalSystemBarRootLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mFitKeyboardExpanded = false
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val r = Rect(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight,
            insets.systemWindowInsetBottom
        )
        FitBothSideBarHelper.setRootInsetsCache(
            Rect(
                0,
                r.top,
                0,
                if (mFitKeyboardExpanded) 0 else r.bottom
            )
        )
        setPadding(r.left, 0, r.right, 0)
        return insets
    }

    fun setFitKeyboardExpanded(fit: Boolean) {
        mFitKeyboardExpanded = fit
        ViewCompat.requestApplyInsets(this)
        requestLayout()
    }
}

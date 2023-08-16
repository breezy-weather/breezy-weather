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

package org.breezyweather.common.ui.widgets.insets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.core.widget.NestedScrollView
import org.breezyweather.R
import org.breezyweather.common.basic.insets.FitBothSideBarHelper
import org.breezyweather.common.basic.insets.FitBothSideBarView
import org.breezyweather.common.basic.insets.FitBothSideBarView.FitSide

class FitSystemBarNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr), FitBothSideBarView {
    private val mHelper: FitBothSideBarHelper

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.FitSystemBarNestedScrollView, defStyleAttr, 0
        )
        val fitSide = a.getInt(
            R.styleable.FitSystemBarNestedScrollView_sv_side,
            FitBothSideBarView.SIDE_TOP or FitBothSideBarView.SIDE_BOTTOM
        )
        a.recycle()
        mHelper = FitBothSideBarHelper(this, fitSide)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return mHelper.onApplyWindowInsets(insets)
    }

    @Deprecated("Deprecated in Java")
    public override fun fitSystemWindows(insets: Rect): Boolean {
        return mHelper.fitSystemWindows(insets)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setPadding(0, mHelper.top(), 0, mHelper.bottom())
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun addFitSide(@FitSide side: Int) {
        mHelper.addFitSide(side)
    }

    override fun removeFitSide(@FitSide side: Int) {
        mHelper.removeFitSide(side)
    }

    override fun setFitSystemBarEnabled(top: Boolean, bottom: Boolean) {
        mHelper.setFitSystemBarEnabled(top, bottom)
    }

    override val topWindowInset: Int
        get() = mHelper.top()
    override val bottomWindowInset: Int
        get() = mHelper.bottom()
}

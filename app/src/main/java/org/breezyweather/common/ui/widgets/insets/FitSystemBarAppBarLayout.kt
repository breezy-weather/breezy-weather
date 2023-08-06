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

package org.breezyweather.common.ui.widgets.insets

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import org.breezyweather.common.basic.insets.FitBothSideBarHelper
import org.breezyweather.common.basic.insets.FitBothSideBarView
import org.breezyweather.common.basic.insets.FitBothSideBarView.FitSide
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.theme.ThemeManager

class FitSystemBarAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr), FitBothSideBarView {

    private val mHelper: FitBothSideBarHelper

    init {
        ViewCompat.setOnApplyWindowInsetsListener(this, null)
        mHelper = FitBothSideBarHelper(
            this,
            FitBothSideBarView.SIDE_TOP
        )
    }

    fun injectDefaultSurfaceTintColor() {
        setBackgroundColor(
            ColorUtils.getWidgetSurfaceColor(
                6f,
                ThemeManager.getInstance(context).getThemeColor(context, androidx.appcompat.R.attr.colorPrimary),
                ThemeManager.getInstance(context).getThemeColor(context, com.google.android.material.R.attr.colorSurface)
            )
        )
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return mHelper.onApplyWindowInsets(insets) { fitSystemBar() }
    }

    private fun fitSystemBar() {
        setPadding(0, mHelper.top(), 0, 0)
    }

    override fun addFitSide(@FitSide side: Int) {
        // do nothing.
    }

    override fun removeFitSide(@FitSide side: Int) {
        // do nothing.
    }

    override fun setFitSystemBarEnabled(top: Boolean, bottom: Boolean) {
        mHelper.setFitSystemBarEnabled(top, bottom)
    }

    override val topWindowInset: Int
        get() = mHelper.top()

    override val bottomWindowInset: Int
        get() = 0
}
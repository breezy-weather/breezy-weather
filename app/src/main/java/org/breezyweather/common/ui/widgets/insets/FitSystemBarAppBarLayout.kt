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
import android.util.AttributeSet
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import org.breezyweather.common.extensions.doOnApplyWindowInsets
import org.breezyweather.common.utils.ColorUtils
import org.breezyweather.theme.ThemeManager

class FitSystemBarAppBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppBarLayout(context, attrs, defStyleAttr) {

    init {
        this.doOnApplyWindowInsets { view, insets ->
            view.updatePadding(
                top = insets.top,
                left = insets.left,
                right = insets.right
            )
        }
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
}

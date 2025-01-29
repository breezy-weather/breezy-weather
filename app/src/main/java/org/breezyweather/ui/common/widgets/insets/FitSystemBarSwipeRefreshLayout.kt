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

package org.breezyweather.ui.common.widgets.insets

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.breezyweather.R

class FitSystemBarSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SwipeRefreshLayout(context, attrs) {

    /**
     * Positions the refresh indicator below the TopAppBar.
     */
    fun fitSystemBar(topInset: Int) {
        val startPosition = topInset + resources.getDimensionPixelSize(R.dimen.normal_margin)
        val endPosition = (startPosition + 64 * resources.displayMetrics.density).toInt()
        if (startPosition != progressViewStartOffset || endPosition != progressViewEndOffset) {
            setProgressViewOffset(false, startPosition, endPosition)
        }
    }
}

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

package org.breezyweather.ui.main.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import org.breezyweather.common.extensions.dpToPx
import org.breezyweather.ui.common.widgets.trend.TrendLayoutManager
import kotlin.math.abs
import kotlin.math.max

class TrendHorizontalLinearLayoutManager @JvmOverloads constructor(
    private val mContext: Context,
    private val mFillCount: Int = 0,
) : TrendLayoutManager(mContext) {
    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        val consumed = super.scrollHorizontallyBy(dx, recycler, state)
        if (consumed == 0) {
            return 0
        } else if (abs(consumed) < abs(dx)) {
            return dx
        }
        return consumed
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (mFillCount > 0) {
            val minWidth = mContext.dpToPx(MIN_ITEM_WIDTH.toFloat()).toInt()
            val minHeight = mContext.dpToPx(MIN_ITEM_HEIGHT.toFloat()).toInt()
            RecyclerView.LayoutParams(
                max(minWidth, width / mFillCount),
                if (height > minHeight) ViewGroup.LayoutParams.MATCH_PARENT else minHeight
            )
        } else {
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return generateDefaultLayoutParams()
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return generateDefaultLayoutParams()
    }

    companion object {
        private const val MIN_ITEM_WIDTH = 56
        private const val MIN_ITEM_HEIGHT = 144
    }
}

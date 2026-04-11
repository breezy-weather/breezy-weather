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

package org.breezyweather.ui.common.decorations

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import org.breezyweather.R

class GridMarginsDecoration(
    @field:Px @param:Px private val mMarginsVertical: Float,
    @field:Px @param:Px private val mMarginsHorizontal: Float,
    recyclerView: RecyclerView,
) : ItemDecoration() {
    constructor(
        context: Context,
        recyclerView: RecyclerView,
    ) : this(context.resources.getDimensionPixelSize(R.dimen.small_margin).toFloat(), recyclerView)
    constructor(@Px margins: Float, recyclerView: RecyclerView) : this(margins, margins, recyclerView)

    init {
        recyclerView.clipToPadding = false
        recyclerView.setPadding(
            (mMarginsHorizontal / 2).toInt(),
            (mMarginsVertical / 2).toInt(),
            (mMarginsHorizontal / 2).toInt(),
            (mMarginsVertical / 2).toInt()
        )
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(
            (mMarginsHorizontal / 2).toInt(),
            (mMarginsVertical / 2).toInt(),
            (mMarginsHorizontal / 2).toInt(),
            (mMarginsVertical / 2).toInt()
        )
    }
}

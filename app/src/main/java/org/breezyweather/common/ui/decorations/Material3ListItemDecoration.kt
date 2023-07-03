package org.breezyweather.common.ui.decorations

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import org.breezyweather.R

class Material3ListItemDecoration(context: Context) : ItemDecoration() {
    @Px
    private val margins: Int = context.resources.getDimensionPixelSize(R.dimen.little_margin)

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(margins, 0, margins, margins);
    }
}
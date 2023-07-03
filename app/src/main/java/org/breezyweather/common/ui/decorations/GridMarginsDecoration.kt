package org.breezyweather.common.ui.decorations

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
    recyclerView: RecyclerView
) : ItemDecoration() {
    constructor(context: Context, recyclerView: RecyclerView) : this(context.resources.getDimensionPixelSize(R.dimen.little_margin).toFloat(), recyclerView)
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
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
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
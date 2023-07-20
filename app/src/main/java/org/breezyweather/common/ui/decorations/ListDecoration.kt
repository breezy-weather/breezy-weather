package org.breezyweather.common.ui.decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import org.breezyweather.common.extensions.dpToPx

/**
 * List decoration.
 */
class ListDecoration(context: Context, @ColorInt colorP: Int) : ItemDecoration() {
    private val mPaint: Paint

    @Px
    private val mDividerDistance: Int

    init {
        mDividerDistance = context.dpToPx(1f).toInt()
        mPaint = Paint().apply {
            color = colorP
            style = Paint.Style.STROKE
            strokeWidth = mDividerDistance.toFloat()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            c.drawLine(
                child.left.toFloat(),
                child.bottom + mDividerDistance / 2f,
                child.right.toFloat(),
                child.bottom + mDividerDistance / 2f,
                mPaint
            )
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, mDividerDistance)
    }

    @get:ColorInt
    var color: Int
        get() = mPaint.color
        set(color) {
            mPaint.color = color
        }
}

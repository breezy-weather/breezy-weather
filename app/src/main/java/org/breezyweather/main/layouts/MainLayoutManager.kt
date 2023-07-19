package org.breezyweather.main.layouts

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

class MainLayoutManager : RecyclerView.LayoutManager() {
    @get:Px
    @Px
    var scrollOffset = 0
        private set

    @Px
    private var mMeasuredHeight = 0
    private var mDataSetChanged = true

    override fun onDetachedFromWindow(view: RecyclerView, recycler: Recycler) {
        super.onDetachedFromWindow(view, recycler)
        removeAndRecycleAllViews(recycler)
    }

    override fun generateDefaultLayoutParams() = RecyclerView.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        mDataSetChanged = true
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        mDataSetChanged = true
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (mDataSetChanged) {
            removeAndRecycleAllViews(recycler)
        } else {
            detachAndScrapAttachedViews(recycler)
        }
        if (state.itemCount == 0 || state.isPreLayout) {
            return
        }
        if (itemCount == 0) {
            return
        }
        var y = 0
        if (!clipToPadding) {
            y += paddingTop
        }
        var childHeight: Int
        var params: MarginLayoutParams
        for (i in 0..<itemCount) {
            val child = recycler.getViewForPosition(i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            childHeight = getDecoratedMeasuredHeight(child)
            params = child.layoutParams as MarginLayoutParams
            layoutDecoratedWithMargins(
                child,
                paddingLeft,
                y,
                width - paddingRight,
                y + childHeight + params.topMargin + params.bottomMargin
            )
            y += childHeight + params.topMargin + params.bottomMargin
        }
        if (!clipToPadding) {
            y += paddingBottom
        }
        mMeasuredHeight = y
        if (mDataSetChanged) {
            scrollOffset = 0
            mDataSetChanged = false
        } else {
            val oldOffset = scrollOffset
            scrollOffset = 0
            scrollVerticallyBy(oldOffset, recycler, state)
        }
    }

    override fun canScrollVertically() = true

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        // dy : + ===> content scroll up.   / show bottom content.
        //      - ===> content scroll down. / show top content.
        if (childCount == 0 || dy == 0) return 0
        var consumed = dy
        if (scrollOffset + consumed + height > mMeasuredHeight) {
            consumed = mMeasuredHeight - scrollOffset - height
        } else if (scrollOffset + consumed < 0) {
            consumed = -scrollOffset
        }
        scrollOffset += consumed
        offsetChildrenVertical(-consumed)
        return consumed
    }

    override fun computeVerticalScrollOffset(state: RecyclerView.State) = scrollOffset

    override fun computeVerticalScrollRange(state: RecyclerView.State) = mMeasuredHeight

    override fun computeVerticalScrollExtent(state: RecyclerView.State) = height
}
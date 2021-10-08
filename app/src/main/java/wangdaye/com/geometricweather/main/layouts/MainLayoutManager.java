package wangdaye.com.geometricweather.main.layouts;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

public class MainLayoutManager extends RecyclerView.LayoutManager {

    private @Px int mScrollOffset;
    private @Px int mMeasuredHeight;
    private boolean mDataSetChanged;

    public MainLayoutManager() {
        super();
        mScrollOffset = 0;
        mMeasuredHeight = 0;
        mDataSetChanged = true;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        removeAndRecycleAllViews(recycler);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAdapterChanged(@Nullable RecyclerView.Adapter oldAdapter, @Nullable RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        mDataSetChanged = true;
    }

    @Override
    public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        mDataSetChanged = true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mDataSetChanged) {
            removeAndRecycleAllViews(recycler);
        } else {
            detachAndScrapAttachedViews(recycler);
        }

        if (state.getItemCount() == 0 || state.isPreLayout()) {
            return;
        }
        if (getItemCount() == 0) {
            return;
        }

        int y = 0;
        if (!getClipToPadding()) {
            y += getPaddingTop();
        }

        int childHeight;
        ViewGroup.MarginLayoutParams params;
        for (int i = 0; i < getItemCount(); i ++) {
            View child = recycler.getViewForPosition(i);
            addView(child);

            measureChildWithMargins(child, 0, 0);
            childHeight = getDecoratedMeasuredHeight(child);
            params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            layoutDecoratedWithMargins(
                    child,
                    getPaddingLeft(),
                    y,
                    getWidth() - getPaddingRight(),
                    y + childHeight + params.topMargin + params.bottomMargin
            );

            y += childHeight + params.topMargin + params.bottomMargin;
        }

        if (!getClipToPadding()) {
            y += getPaddingBottom();
        }

        mMeasuredHeight = y;

        if (mDataSetChanged) {
            mScrollOffset = 0;
            mDataSetChanged = false;
        } else {
            int oldOffset = mScrollOffset;
            mScrollOffset = 0;
            scrollVerticallyBy(oldOffset, recycler, state);
        }
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // dy : + ===> content scroll up.   / show bottom content.
        //      - ===> content scroll down. / show top content.
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }

        int consumed = dy;
        if (mScrollOffset + consumed + getHeight() > mMeasuredHeight) {
            consumed = mMeasuredHeight - mScrollOffset - getHeight();
        } else if (mScrollOffset + consumed < 0) {
            consumed = -mScrollOffset;
        }
        mScrollOffset += consumed;

        offsetChildrenVertical(-consumed);
        return consumed;
    }

    @Px
    public int getScrollOffset() {
        return mScrollOffset;
    }

    @Override
    public int computeVerticalScrollOffset(@NonNull RecyclerView.State state) {
        return mScrollOffset;
    }

    @Override
    public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
        return mMeasuredHeight;
    }

    @Override
    public int computeVerticalScrollExtent(@NonNull RecyclerView.State state) {
        return getHeight();
    }
}
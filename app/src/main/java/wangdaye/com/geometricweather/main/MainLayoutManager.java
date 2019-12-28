package wangdaye.com.geometricweather.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.utils.DisplayUtils;

public class MainLayoutManager extends RecyclerView.LayoutManager {

    private Context context;
    private @Px int scrollOffset;
    private @Px int measuredHeight;
    private boolean dataSetChanged;

    public MainLayoutManager(Context context) {
        super();
        this.context = context;
        this.scrollOffset = 0;
        this.measuredHeight = 0;
        this.dataSetChanged = true;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        removeAndRecycleAllViews(recycler);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onAdapterChanged(@Nullable RecyclerView.Adapter oldAdapter, @Nullable RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        this.dataSetChanged = true;
    }

    @Override
    public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        this.dataSetChanged = true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

        if (dataSetChanged) {
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

        int cardWidth = DisplayUtils.getTabletListAdaptiveWidth(context, getWidth());
        int widthUsed = 0;
        if (cardWidth < getWidth()) {
            widthUsed = getWidth() - cardWidth;
        }

        int y = 0;
        if (!getClipToPadding()) {
            y += getPaddingTop();
        }

        int childWidth;
        int childHeight;
        ViewGroup.MarginLayoutParams params;
        for (int i = 0; i < getItemCount(); i ++) {
            View child = recycler.getViewForPosition(i);
            addView(child);

            measureChildWithMargins(child, widthUsed, 0);
            childWidth = getDecoratedMeasuredWidth(child);
            childHeight = getDecoratedMeasuredHeight(child);
            params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            layoutDecoratedWithMargins(
                    child,
                    widthUsed / 2,
                    y,
                    widthUsed / 2 + childWidth + params.leftMargin + params.rightMargin,
                    y + childHeight + params.topMargin + params.bottomMargin
            );

            y += childHeight + params.topMargin + params.bottomMargin;
        }

        if (!getClipToPadding()) {
            y += getPaddingBottom();
        }

        measuredHeight = y;

        if (dataSetChanged) {
            scrollOffset = 0;
            dataSetChanged = false;
        } else {
            int oldOffset = scrollOffset;
            scrollOffset = 0;
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
        if (scrollOffset + consumed + getHeight() > measuredHeight) {
            consumed = measuredHeight - scrollOffset - getHeight();
        } else if (scrollOffset + consumed < 0) {
            consumed = -scrollOffset;
        }
        scrollOffset += consumed;

        offsetChildrenVertical(-consumed);
        return consumed;
    }

    @Px
    public int getScrollOffset() {
        return scrollOffset;
    }

    @Override
    public int computeVerticalScrollOffset(@NonNull RecyclerView.State state) {
        return scrollOffset;
    }

    @Override
    public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
        return measuredHeight;
    }

    @Override
    public int computeVerticalScrollExtent(@NonNull RecyclerView.State state) {
        return getHeight();
    }
}
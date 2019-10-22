package wangdaye.com.geometricweather.main.ui;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class MainLayoutManager extends RecyclerView.LayoutManager {

    private int scrollOffset;
    private int measuredHeight;

    public MainLayoutManager() {
        super();
        this.scrollOffset = 0;
        this.measuredHeight = 0;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        detachAndScrapAttachedViews(recycler);

        if (getItemCount() == 0) {
            return;
        }

        int y = 0;
        int childWidth;
        int childHeight;
        ViewGroup.MarginLayoutParams params;
        for (int i = 0; i < getItemCount(); i ++) {
            View child = recycler.getViewForPosition(i);
            addView(child);

            measureChildWithMargins(child, 0, 0);
            childWidth = getDecoratedMeasuredWidth(child);
            childHeight = getDecoratedMeasuredHeight(child);
            params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            layoutDecoratedWithMargins(
                    child,
                    0,
                    y,
                    childWidth + params.leftMargin + params.rightMargin,
                    y + childHeight + params.topMargin + params.bottomMargin
            );

            y += childHeight + params.topMargin + params.bottomMargin;
        }

        scrollOffset = 0;
        measuredHeight = y;
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
}

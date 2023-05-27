package wangdaye.com.geometricweather.common.ui.widgets.slidingItem;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SlidingItemTouchCallback extends ItemTouchHelper.Callback {

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END
        );
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        SlidingItemContainerLayout slidingContainer = findSlidingContainer(viewHolder);

        if (isCurrentlyActive) {
            Object originalElevation = slidingContainer.getTag(androidx.viewpager2.R.id.item_touch_helper_previous_elevation);
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(slidingContainer);
                float newElevation = 1.0F + findMaxElevation(recyclerView, slidingContainer);
                ViewCompat.setElevation(slidingContainer, newElevation);
                slidingContainer.setTag(androidx.viewpager2.R.id.item_touch_helper_previous_elevation, originalElevation);
            }
        }

        viewHolder.itemView.setTranslationY(dY);
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            slidingContainer.swipe(dX);
        }
    }

    private static float findMaxElevation(RecyclerView recyclerView, View itemView) {
        int childCount = recyclerView.getChildCount();
        float max = 0.0F;

        for(int i = 0; i < childCount; ++i) {
            View child = recyclerView.getChildAt(i);
            if (child != itemView) {
                float elevation = ViewCompat.getElevation(child);
                if (elevation > max) {
                    max = elevation;
                }
            }
        }

        return max;
    }

    private static SlidingItemContainerLayout findSlidingContainer(
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        if (viewHolder.itemView instanceof SlidingItemContainerLayout) {
            return (SlidingItemContainerLayout) viewHolder.itemView;
        }

        if (viewHolder.itemView instanceof CardView) {
            return (SlidingItemContainerLayout) ((CardView) viewHolder.itemView).getChildAt(0);
        }

        throw new IllegalStateException("Cannot find a valid sliding container.");
    }
}
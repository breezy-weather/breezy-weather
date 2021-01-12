package wangdaye.com.geometricweather.ui.widget.slidingItem;

import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.R;

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
        if (Build.VERSION.SDK_INT >= 21 && isCurrentlyActive) {
            Object originalElevation = viewHolder.itemView.getTag(R.id.item_touch_helper_previous_elevation);
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(viewHolder.itemView);
                float newElevation = 1.0F + findMaxElevation(recyclerView, viewHolder.itemView);
                ViewCompat.setElevation(viewHolder.itemView, newElevation);
                viewHolder.itemView.setTag(R.id.item_touch_helper_previous_elevation, originalElevation);
            }
        }

        viewHolder.itemView.setTranslationY(dY);
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            ((SlidingItemContainerLayout) viewHolder.itemView).swipe(dX);
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
}
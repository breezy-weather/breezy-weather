package wangdaye.com.geometricweather.main.ui.adapter.main;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.main.ui.adapter.main.holder.FooterViewHolder;
import wangdaye.com.geometricweather.main.ui.adapter.main.holder.HeaderViewHolder;

public class MainItemTouchCallback extends ItemTouchHelper.Callback {

    private MainAdapter adapter;

    public MainItemTouchCallback(MainAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof HeaderViewHolder || viewHolder instanceof FooterViewHolder) {
            return makeMovementFlags(0, 0);
        } else {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // do nothing.
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        adapter.dragSort(recyclerView, viewHolder, target);
        return true;
    }

    @Override
    public boolean canDropOver(@NonNull RecyclerView recyclerView,
                               @NonNull RecyclerView.ViewHolder current,
                               @NonNull RecyclerView.ViewHolder target) {
        return super.canDropOver(recyclerView, current, target);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }
}

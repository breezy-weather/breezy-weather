package wangdaye.com.geometricweather.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class SyncListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private @NonNull List<T> modelList;
    private final @NonNull DiffUtil.ItemCallback<T> callback;

    public SyncListAdapter(@NonNull List<T> list, @NonNull DiffUtil.ItemCallback<T> callback) {
        this.modelList = list;
        this.callback = callback;
    }

    public void submitList(@NonNull List<T> newList) {
        if (newList == modelList) {
            return;
        }

        final List<T> oldList = getCurrentList();

        if (oldList.size() == 0 && newList.size() == 0) {
            return;
        }

        if (oldList.size() == 0) {
            int insertedCount = newList.size();
            this.modelList = newList;
            notifyItemRangeInserted(0, insertedCount);
            return;
        }

        if (newList.size() == 0) {
            int removedCount = oldList.size();
            this.modelList = newList;
            notifyItemRangeRemoved(0, removedCount);
            return;
        }

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return callback.areItemsTheSame(
                        oldList.get(oldItemPosition),
                        newList.get(newItemPosition)
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return callback.areContentsTheSame(
                        oldList.get(oldItemPosition),
                        newList.get(newItemPosition)
                );
            }
        }, false);

        modelList = newList;
        result.dispatchUpdatesTo(this);
    }

    public void submitMove(int from, int to) {
        Collections.swap(modelList, from, to);
        notifyItemMoved(from, to);
    }

    public List<T> getCurrentList() {
        return Collections.unmodifiableList(modelList);
    }

    public T getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}

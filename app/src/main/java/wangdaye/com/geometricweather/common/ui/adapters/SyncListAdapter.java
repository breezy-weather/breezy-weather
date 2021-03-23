package wangdaye.com.geometricweather.common.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public abstract class SyncListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private @NonNull List<T> mModelList;
    private final @NonNull DiffUtil.ItemCallback<T> mCallback;

    public SyncListAdapter(@NonNull List<T> list, @NonNull DiffUtil.ItemCallback<T> callback) {
        mModelList = list;
        mCallback = callback;
    }

    public void submitList(@NonNull List<T> newList) {
        if (newList == mModelList) {
            return;
        }

        final List<T> oldList = getCurrentList();

        if (oldList.size() == 0 && newList.size() == 0) {
            return;
        }

        if (oldList.size() == 0) {
            int insertedCount = newList.size();
            mModelList = newList;
            notifyItemRangeInserted(0, insertedCount);
            return;
        }

        if (newList.size() == 0) {
            int removedCount = oldList.size();
            mModelList = newList;
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
                return mCallback.areItemsTheSame(
                        oldList.get(oldItemPosition),
                        newList.get(newItemPosition)
                );
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return mCallback.areContentsTheSame(
                        oldList.get(oldItemPosition),
                        newList.get(newItemPosition)
                );
            }
        }, false);

        mModelList = newList;
        result.dispatchUpdatesTo(this);
    }

    public void submitMove(int from, int to) {
        Collections.swap(mModelList, from, to);
        notifyItemMoved(from, to);
    }

    public List<T> getCurrentList() {
        return Collections.unmodifiableList(mModelList);
    }

    public T getItem(int position) {
        return mModelList.get(position);
    }

    @Override
    public int getItemCount() {
        return mModelList.size();
    }
}

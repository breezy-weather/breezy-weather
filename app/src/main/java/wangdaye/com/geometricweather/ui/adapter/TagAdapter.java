package wangdaye.com.geometricweather.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.ui.widget.TagView;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private List<Tag> tagList;
    private OnTagCheckedListener listener;
    private int checkedIndex;

    public static final int UNCHECKABLE_INDEX = -1;

    class ViewHolder extends RecyclerView.ViewHolder {

        private TagView tagView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagView = itemView.findViewById(R.id.item_tag);
            tagView.setOnClickListener(v -> {
                boolean consumed = false;
                if (listener != null) {
                    consumed = listener.onItemChecked(!tagView.isChecked(), checkedIndex, getAdapterPosition());
                }
                if (!consumed && checkedIndex != getAdapterPosition()) {
                    int i = checkedIndex;
                    checkedIndex = getAdapterPosition();
                    notifyItemChanged(i);
                    notifyItemChanged(checkedIndex);
                }
            });
        }

        void onBindView(Tag tag, boolean checked) {
            tagView.setText(tag.getName());
            setChecked(checked);
        }

        public void setChecked(boolean checked) {
            tagView.setChecked(checked);
        }
    }

    public TagAdapter(List<Tag> tagList, OnTagCheckedListener listener) {
        this(tagList, listener, UNCHECKABLE_INDEX);
    }

    public TagAdapter(List<Tag> tagList, OnTagCheckedListener listener, int checkedIndex) {
        this.tagList = tagList;
        this.listener = listener;
        this.checkedIndex = checkedIndex;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_tag, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(tagList.get(position), position == checkedIndex);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public void insertItem(Tag tag) {
        tagList.add(tag);
        notifyItemInserted(tagList.size() - 1);
    }

    public Tag removeItem(int position) {
        Tag tag = tagList.remove(position);
        notifyItemRemoved(position);
        return tag;
    }

    public interface OnTagCheckedListener {
        boolean onItemChecked(boolean checked, int oldPosition, int newPosition);
    }

    public interface Tag {
        String getName();
    }
}

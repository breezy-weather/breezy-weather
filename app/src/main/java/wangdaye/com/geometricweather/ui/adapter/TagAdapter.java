package wangdaye.com.geometricweather.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.ui.widget.TagView;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private List<Tag> tagList;
    private @ColorInt int checkedBackgroundColor;
    private OnTagCheckedListener listener;
    private @Nullable MainThemePicker picker;
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

            if (picker != null) {
                tagView.setCheckedBackgroundColor(checkedBackgroundColor);
                tagView.setUncheckedBackgroundColor(picker.getLineColor(tagView.getContext()));
            }
        }

        public void setChecked(boolean checked) {
            tagView.setChecked(checked);
            if (picker != null) {
                if (checked) {
                    tagView.setTextColor(picker.getTextContentColor(tagView.getContext()));
                } else {
                    tagView.setTextColor(picker.getTextSubtitleColor(tagView.getContext()));
                }
            }
        }
    }

    public TagAdapter(List<Tag> tagList, OnTagCheckedListener listener) {
        this(tagList, Color.TRANSPARENT, listener, null, UNCHECKABLE_INDEX);
    }

    public TagAdapter(List<Tag> tagList, @ColorInt int checkedBackgroundColor,
                      OnTagCheckedListener listener, @Nullable MainThemePicker picker,
                      int checkedIndex) {
        this.tagList = tagList;
        this.checkedBackgroundColor = checkedBackgroundColor;
        this.listener = listener;
        this.picker = picker;
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

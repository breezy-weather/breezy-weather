package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
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
import wangdaye.com.geometricweather.ui.widget.TagView;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private List<Tag> tagList;
    private @ColorInt int checkedBackgroundColor;
    private OnTagCheckedListener listener;
    private @Nullable ThemeManager themeManager;
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

            if (themeManager != null) {
                tagView.setCheckedBackgroundColor(checkedBackgroundColor);
                tagView.setUncheckedBackgroundColor(themeManager.getLineColor(tagView.getContext()));
            }
        }

        public void setChecked(boolean checked) {
            tagView.setChecked(checked);
            if (themeManager != null) {
                if (checked) {
                    tagView.setTextColor(themeManager.getTextContentColor(tagView.getContext()));
                } else {
                    tagView.setTextColor(themeManager.getTextSubtitleColor(tagView.getContext()));
                }
            }
        }
    }

    public TagAdapter(Context context, List<Tag> tagList, OnTagCheckedListener listener) {
        this(context, tagList, Color.TRANSPARENT, listener, UNCHECKABLE_INDEX);
    }

    public TagAdapter(Context context, List<Tag> tagList, @ColorInt int checkedBackgroundColor,
                      OnTagCheckedListener listener, int checkedIndex) {
        this.tagList = tagList;
        this.checkedBackgroundColor = checkedBackgroundColor;
        this.listener = listener;
        this.themeManager = ThemeManager.getInstance(context);
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

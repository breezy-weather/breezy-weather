package org.breezyweather.settings.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.breezyweather.R;
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay;
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemContainerLayout;

import java.util.List;

public class DetailDisplayAdapter extends RecyclerView.Adapter<DetailDisplayAdapter.ViewHolder> {

    private final List<DetailDisplay> mDetailDisplayList;
    private final OnItemRemoveListener mRemoveListener;
    private final OnItemDragListener mDragListener;

    public interface OnItemRemoveListener {
        void onRemoved(DetailDisplay detailDisplay);
    }

    public interface OnItemDragListener {
        void onDrag(ViewHolder holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SlidingItemContainerLayout container;
        RelativeLayout item;
        TextView title;
        ImageButton sortButton;
        ImageButton deleteButton;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_card_display_container);
            item = itemView.findViewById(R.id.item_card_display);
            title = itemView.findViewById(R.id.item_card_display_title);
            sortButton = itemView.findViewById(R.id.item_card_display_sortButton);
            sortButton.setOnTouchListener((View v, MotionEvent event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDragListener.onDrag(this);
                }
                return false;
            });
            deleteButton = itemView.findViewById(R.id.item_card_display_deleteBtn);
            deleteButton.setOnClickListener(v -> removeItem(getBindingAdapterPosition()));
        }

        void onBindView(DetailDisplay detailDisplay) {
            title.setText(detailDisplay.getName(title.getContext()));

            container.swipe(0);
            container.setOnClickListener(v -> {
                // do nothing.
            });
        }
    }

    public DetailDisplayAdapter(List<DetailDisplay> detailDisplayList,
                                OnItemRemoveListener removeListener,
                                OnItemDragListener dragListener) {
        mDetailDisplayList = detailDisplayList;
        mRemoveListener = removeListener;
        mDragListener = dragListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mDetailDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDetailDisplayList.size();
    }

    public List<DetailDisplay> getDetailDisplayList() {
        return mDetailDisplayList;
    }

    public void insertItem(DetailDisplay detailDisplay) {
        mDetailDisplayList.add(detailDisplay);
        notifyItemInserted(mDetailDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        DetailDisplay detailDisplay = mDetailDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        mRemoveListener.onRemoved(detailDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        mDetailDisplayList.add(toPosition, mDetailDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }
}

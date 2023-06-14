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

import java.util.List;

import org.breezyweather.common.basic.models.options.appearance.CardDisplay;
import org.breezyweather.common.ui.widgets.slidingItem.SlidingItemContainerLayout;
import org.breezyweather.R;

public class CardDisplayAdapter extends RecyclerView.Adapter<CardDisplayAdapter.ViewHolder> {

    private final List<CardDisplay> mCardDisplayList;
    private final OnItemRemoveListener mRemoveListener;
    private final OnItemDragListener mDragListener;

    public interface OnItemRemoveListener {
        void onRemoved(CardDisplay cardDisplay);
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

        void onBindView(CardDisplay cardDisplay) {
            title.setText(cardDisplay.getName(title.getContext()));
            
            container.swipe(0);
            container.setOnClickListener(v -> {
                // do nothing.
            });
        }
    }

    public CardDisplayAdapter(List<CardDisplay> cardDisplayList,
                              OnItemRemoveListener removeListener,
                              OnItemDragListener dragListener) {
        mCardDisplayList = cardDisplayList;
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
        holder.onBindView(mCardDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCardDisplayList.size();
    }

    public List<CardDisplay> getCardDisplayList() {
        return mCardDisplayList;
    }

    public void insertItem(CardDisplay cardDisplay) {
        mCardDisplayList.add(cardDisplay);
        notifyItemInserted(mCardDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        CardDisplay cardDisplay = mCardDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        mRemoveListener.onRemoved(cardDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        mCardDisplayList.add(toPosition, mCardDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }
}

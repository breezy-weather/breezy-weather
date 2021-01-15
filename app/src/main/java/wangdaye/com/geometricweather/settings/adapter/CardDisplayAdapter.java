package wangdaye.com.geometricweather.settings.adapter;

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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.ui.widget.slidingItem.SlidingItemContainerLayout;

public class CardDisplayAdapter extends RecyclerView.Adapter<CardDisplayAdapter.ViewHolder> {

    private final List<CardDisplay> cardDisplayList;
    private final OnItemRemoveListener removeListener;
    private final OnItemDragListener dragListener;

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
                    dragListener.onDrag(this);
                }
                return false;
            });
            deleteButton = itemView.findViewById(R.id.item_card_display_deleteBtn);
            deleteButton.setOnClickListener(v -> removeItem(getAdapterPosition()));
        }

        void onBindView(CardDisplay cardDisplay) {
            title.setText(cardDisplay.getCardName(title.getContext()));
            
            container.swipe(0);
            container.setOnClickListener(v -> {
                // do nothing.
            });
        }
    }

    public CardDisplayAdapter(List<CardDisplay> cardDisplayList,
                              OnItemRemoveListener removeListener,
                              OnItemDragListener dragListener) {
        this.cardDisplayList = cardDisplayList;
        this.removeListener = removeListener;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(cardDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return cardDisplayList.size();
    }

    public List<CardDisplay> getCardDisplayList() {
        return cardDisplayList;
    }

    public void insertItem(CardDisplay cardDisplay) {
        cardDisplayList.add(cardDisplay);
        notifyItemInserted(cardDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        CardDisplay cardDisplay = cardDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        removeListener.onRemoved(cardDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        cardDisplayList.add(toPosition, cardDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    public interface OnItemRemoveListener {
        void onRemoved(CardDisplay cardDisplay);
    }

    public interface OnItemDragListener {
        void onDrag(ViewHolder holder);
    }
}

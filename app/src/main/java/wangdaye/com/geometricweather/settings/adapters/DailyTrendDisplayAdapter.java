package wangdaye.com.geometricweather.settings.adapters;

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
import wangdaye.com.geometricweather.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.ui.widgets.slidingItem.SlidingItemContainerLayout;

public class DailyTrendDisplayAdapter extends RecyclerView.Adapter<DailyTrendDisplayAdapter.ViewHolder> {

    private final List<DailyTrendDisplay> mDailyTrendDisplayList;
    private final OnItemRemoveListener mRemoveListener;
    private final OnItemDragListener mDragListener;

    public interface OnItemRemoveListener {
        void onRemoved(DailyTrendDisplay dailyTrendDisplay);
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
            deleteButton.setOnClickListener(v -> removeItem(getAdapterPosition()));
        }

        void onBindView(DailyTrendDisplay dailyTrendDisplay) {
            title.setText(dailyTrendDisplay.getTagName(title.getContext()));

            container.swipe(0);
            container.setOnClickListener(v -> {
                // do nothing.
            });
        }
    }

    public DailyTrendDisplayAdapter(List<DailyTrendDisplay> dailyTrendDisplayList,
                                    OnItemRemoveListener removeListener,
                                    OnItemDragListener dragListener) {
        mDailyTrendDisplayList = dailyTrendDisplayList;
        mRemoveListener = removeListener;
        mDragListener = dragListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_card_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(mDailyTrendDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDailyTrendDisplayList.size();
    }

    public List<DailyTrendDisplay> getDailyTrendDisplayList() {
        return mDailyTrendDisplayList;
    }

    public void insertItem(DailyTrendDisplay dailyTrendDisplay) {
        mDailyTrendDisplayList.add(dailyTrendDisplay);
        notifyItemInserted(mDailyTrendDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        DailyTrendDisplay dailyTrendDisplay = mDailyTrendDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        mRemoveListener.onRemoved(dailyTrendDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        mDailyTrendDisplayList.add(toPosition, mDailyTrendDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }
}

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
import wangdaye.com.geometricweather.common.basic.models.options.appearance.HourlyTrendDisplay;
import wangdaye.com.geometricweather.common.ui.widgets.slidingItem.SlidingItemContainerLayout;

public class HourlyTrendDisplayAdapter
        extends RecyclerView.Adapter<HourlyTrendDisplayAdapter.ViewHolder> {

    private final List<HourlyTrendDisplay> mHourlyTrendDisplayList;
    private final OnItemRemoveListener mRemoveListener;
    private final OnItemDragListener mDragListener;

    public interface OnItemRemoveListener {
        void onRemoved(HourlyTrendDisplay hourlyTrendDisplay);
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

        void onBindView(HourlyTrendDisplay hourlyTrendDisplay) {
            title.setText(hourlyTrendDisplay.getName(title.getContext()));

            container.swipe(0);
            container.setOnClickListener(v -> {
                // do nothing.
            });
        }
    }

    public HourlyTrendDisplayAdapter(List<HourlyTrendDisplay> hourlyTrendDisplayList,
                                     OnItemRemoveListener removeListener,
                                     OnItemDragListener dragListener) {
        mHourlyTrendDisplayList = hourlyTrendDisplayList;
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
        holder.onBindView(mHourlyTrendDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mHourlyTrendDisplayList.size();
    }

    public List<HourlyTrendDisplay> getHourlyTrendDisplayList() {
        return mHourlyTrendDisplayList;
    }

    public void insertItem(HourlyTrendDisplay hourlyTrendDisplay) {
        mHourlyTrendDisplayList.add(hourlyTrendDisplay);
        notifyItemInserted(mHourlyTrendDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        HourlyTrendDisplay hourlyTrendDisplay = mHourlyTrendDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        mRemoveListener.onRemoved(hourlyTrendDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        mHourlyTrendDisplayList.add(toPosition, mHourlyTrendDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }
}
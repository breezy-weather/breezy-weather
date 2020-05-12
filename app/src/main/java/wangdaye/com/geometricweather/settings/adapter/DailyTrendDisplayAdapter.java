package wangdaye.com.geometricweather.settings.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.utils.DisplayUtils;

public class DailyTrendDisplayAdapter extends RecyclerView.Adapter<DailyTrendDisplayAdapter.ViewHolder> {

    private List<DailyTrendDisplay> dailyTrendDisplayList;
    private OnItemRemoveListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout container;
        AppCompatImageView deleteImageLeft;
        AppCompatImageView deleteImageRight;
        RelativeLayout item;
        TextView title;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_card_display_container);
            deleteImageLeft = itemView.findViewById(R.id.item_card_display_deleteIconLeft);
            deleteImageRight = itemView.findViewById(R.id.item_card_display_deleteIconRight);
            item = itemView.findViewById(R.id.item_card_display);
            title = itemView.findViewById(R.id.item_card_display_title);
            deleteButton = itemView.findViewById(R.id.item_card_display_deleteBtn);
            deleteButton.setOnClickListener(v -> removeItem(getAdapterPosition()));
        }

        void onBindView(DailyTrendDisplay dailyTrendDisplay) {
            title.setText(dailyTrendDisplay.getTagName(title.getContext()));
            drawSwipe(0);
            drawDrag(title.getContext(), false);
        }

        public ViewHolder drawDrag(Context context, boolean elevate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                container.setElevation(DisplayUtils.dpToPx(context, elevate ? 10 : 0));
            }
            return this;
        }

        public ViewHolder drawSwipe(float dX) {
            container.setTranslationX(0);
            item.setTranslationX(dX);
            deleteImageLeft.setTranslationX((float) Math.min(0.5 * (dX - deleteImageLeft.getMeasuredWidth()), 0));
            deleteImageRight.setTranslationX((float) Math.max(0.5 * (dX + deleteImageRight.getMeasuredWidth()), 0));
            return this;
        }
    }

    public DailyTrendDisplayAdapter(List<DailyTrendDisplay> dailyTrendDisplayList, OnItemRemoveListener listener) {
        this.dailyTrendDisplayList = dailyTrendDisplayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_card_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(dailyTrendDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return dailyTrendDisplayList.size();
    }

    public List<DailyTrendDisplay> getDailyTrendDisplayList() {
        return dailyTrendDisplayList;
    }

    public void insertItem(DailyTrendDisplay dailyTrendDisplay) {
        dailyTrendDisplayList.add(dailyTrendDisplay);
        notifyItemInserted(dailyTrendDisplayList.size() - 1);
    }

    public void removeItem(int adapterPosition) {
        DailyTrendDisplay dailyTrendDisplay = dailyTrendDisplayList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        listener.onRemoved(dailyTrendDisplay);
    }

    public void moveItem(int fromPosition, int toPosition) {
        dailyTrendDisplayList.add(toPosition, dailyTrendDisplayList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    public interface OnItemRemoveListener {
        void onRemoved(DailyTrendDisplay dailyTrendDisplay);
    }
}

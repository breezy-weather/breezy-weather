package wangdaye.com.geometricweather.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Location adapter.
 * */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private OnLocationItemClickListener listener = null;

    public List<Location> itemList;
    private boolean manage;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        RelativeLayout locationItemContainer;
        RelativeLayout locationItemView;
        AppCompatImageView deleteImageLeft;
        AppCompatImageView deleteImageRight;
        TextView title;
        TextView subtitle;
        TextView source;
        AppCompatImageButton deleteButton;

        private OnLocationItemClickListener listener;

        ViewHolder(View itemView, OnLocationItemClickListener listener) {
            super(itemView);
            this.locationItemContainer = itemView.findViewById(R.id.item_location_container);
            this.locationItemView = itemView.findViewById(R.id.item_location_item);
            this.deleteImageLeft = itemView.findViewById(R.id.item_location_deleteIconLeft);
            this.deleteImageRight = itemView.findViewById(R.id.item_location_deleteIconRight);
            this.title = itemView.findViewById(R.id.item_location_title);
            this.subtitle = itemView.findViewById(R.id.item_location_subtitle);
            this.source = itemView.findViewById(R.id.item_location_source);
            this.deleteButton = itemView.findViewById(R.id.item_location_deleteBtn);
            this.listener = listener;

            locationItemContainer.setOnClickListener(this);

            deleteButton.setOnClickListener(this);
            if (!manage) {
                deleteButton.setVisibility(View.GONE);
            }
        }

        public ViewHolder drawDrag(Context context, boolean elevate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                locationItemContainer.setElevation(DisplayUtils.dpToPx(context, elevate ? 10 : 0));
            }
            return this;
        }

        public ViewHolder drawSwipe(float dX) {
            locationItemContainer.setTranslationX(0);
            locationItemView.setTranslationX(dX);
            deleteImageLeft.setTranslationX((float) Math.min(0.5 * (dX - deleteImageLeft.getMeasuredWidth()), 0));
            deleteImageRight.setTranslationX((float) Math.max(0.5 * (dX + deleteImageRight.getMeasuredWidth()), 0));
            return this;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_location_container:
                    listener.onClick(v,getAdapterPosition());
                    break;

                case R.id.item_location_deleteBtn:
                    listener.onDelete(v,getAdapterPosition());
                    break;
            }
        }
    }

    public LocationAdapter(Context context, List<Location> itemList, boolean manage, OnLocationItemClickListener l) {
        this.context = context;
        this.itemList = itemList;
        this.manage = manage;
        setOnLocationItemClickListener(l);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view, listener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // title.
        if (itemList.get(position).isLocal()) {
            holder.title.setText(context.getString(R.string.local));
        } else {
            holder.title.setText(itemList.get(position).getCityName(context));
        }

        // subtitle.
        if (!itemList.get(position).isLocal() || itemList.get(position).isUsable()) {
            holder.subtitle.setText(itemList.get(position).country
                    + " " + itemList.get(position).province
                    + (itemList.get(position).province.equals(itemList.get(position).city)
                            ? "" : (" " + itemList.get(position).city))
                    + (itemList.get(position).city.equals(itemList.get(position).district)
                            ? "" : (" " + itemList.get(position).district)));
        } else {
            holder.subtitle.setText(context.getString(R.string.feedback_not_yet_location));
        }

        // source.
        if (itemList.get(position).isLocal() && !itemList.get(position).isUsable()) {
            holder.source.setText("...");
        } else {
            holder.source.setText("Powered by " + ValueUtils.getWeatherSource(context, itemList.get(position).source));
        }

        holder.drawSwipe(0);
        holder.drawDrag(context, false);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void insertData(Location item, int adapterPosition) {
        this.itemList.add(adapterPosition, item);
        this.notifyItemInserted(adapterPosition);
    }

    public void removeData(int adapterPosition) {
        this.itemList.remove(adapterPosition);
        this.notifyItemRemoved(adapterPosition);
    }

    public void moveData(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(this.itemList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(this.itemList, i, i - 1);
            }
        }
        this.notifyItemMoved(fromPosition, toPosition);
    }

    // interface.

    public interface OnLocationItemClickListener {
        void onClick(View view, int position);
        void onDelete(View view, int position);
    }

    private void setOnLocationItemClickListener(OnLocationItemClickListener l){
        this.listener = l;
    }
}

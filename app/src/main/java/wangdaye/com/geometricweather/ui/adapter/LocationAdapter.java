package wangdaye.com.geometricweather.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Location adapter.
 * */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder>
        implements ICustomAdapter {

    private GeoActivity activity;
    private int requestCode;
    private OnLocationItemClickListener listener = null;

    public List<Location> itemList;
    private boolean manage;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        RelativeLayout locationItemContainer;
        RelativeLayout locationItemView;
        AppCompatImageView deleteImageLeft;
        AppCompatImageView deleteImageRight;
        TextView title;
        TextView subtitle;
        TextView source;
        AppCompatImageButton settingsButton;
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
            this.settingsButton = itemView.findViewById(R.id.item_location_settingsBtn);
            this.deleteButton = itemView.findViewById(R.id.item_location_deleteBtn);
            this.listener = listener;

            locationItemContainer.setOnClickListener(this);
            settingsButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
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

        void setSettingsButton(Location location) {
            if (location.isCurrentPosition()) {
                settingsButton.setImageResource(R.drawable.ic_settings);
                settingsButton.setSupportImageTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(activity, R.color.colorTextContent)
                        )
                );
            } else {
                settingsButton.setImageResource(
                        location.isResidentPosition()
                                ? R.drawable.ic_star
                                : R.drawable.ic_star_outline
                );
                settingsButton.setSupportImageTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(
                                        activity,
                                        location.isResidentPosition()
                                                ? R.color.colorTextAlert
                                                : R.color.colorTextContent
                                )
                        )
                );
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_location_container:
                    listener.onClick(v, getAdapterPosition());
                    break;

                case R.id.item_location_settingsBtn:
                    Location location = itemList.get(getAdapterPosition());
                    if (location.isCurrentPosition()) {
                        IntentHelper.startSelectProviderActivityForResult(activity, requestCode);
                    } else {
                        location.setResidentPosition(!location.isResidentPosition());
                        setSettingsButton(location);
                        listener.onResidentSwitch(v, getAdapterPosition(), location.isResidentPosition());
                    }
                    break;

                case R.id.item_location_deleteBtn:
                    listener.onDelete(v, getAdapterPosition());
                    break;
            }
        }
    }

    public LocationAdapter(GeoActivity activity, int requestCode, List<Location> itemList,
                           boolean manage, OnLocationItemClickListener l) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.itemList = itemList;
        this.manage = manage;
        setOnLocationItemClickListener(l);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(activity).inflate(R.layout.item_location, parent, false),
                listener
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = itemList.get(position);
        
        // title.
        if (location.isCurrentPosition()) {
            holder.title.setText(activity.getString(R.string.current_location));
        } else {
            holder.title.setText(location.getCityName(activity));
        }

        // subtitle.
        if (!location.isCurrentPosition() || location.isUsable()) {
            StringBuilder builder = new StringBuilder(
                    location.getCountry() + " " + location.getProvince()
            );
            if (!location.getProvince().equals(location.getCity())) {
                builder.append(" ").append(location.getCity());
            }
            if (!location.getCity().equals(location.getDistrict())) {
                builder.append(" ").append(location.getDistrict());
            }
            holder.subtitle.setText(builder.toString());
        } else {
            holder.subtitle.setText(activity.getString(R.string.feedback_not_yet_location));
        }

        // source.
        if (location.isCurrentPosition() && !location.isUsable()) {
            holder.source.setTextColor(ContextCompat.getColor(activity, R.color.colorTextSubtitle));
            holder.source.setText("...");
        } else {
            holder.source.setTextColor(location.getWeatherSource().getSourceColor());
            holder.source.setText("Powered by " + location.getWeatherSource().getSourceUrl());
        }

        // swipe icon.
        if (manage) {
            holder.settingsButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.setSettingsButton(location);
        } else {
            holder.settingsButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.drawSwipe(0);
        holder.drawDrag(activity, false);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void insertData(Location item, int adapterPosition) {
        itemList.add(adapterPosition, item);
        notifyItemInserted(adapterPosition);
    }

    public void removeData(int adapterPosition) {
        itemList.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
    }

    public void moveData(int fromPosition, int toPosition) {
        itemList.add(toPosition, itemList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    @ColorInt
    public int getItemSourceColor(int position) {
        if (0 <= position && position < itemList.size()) {
            return itemList.get(position).getWeatherSource().getSourceColor();
        } else {
            return Color.TRANSPARENT;
        }
    }

    // interface.

    public interface OnLocationItemClickListener {
        void onClick(View view, int position);
        void onDelete(View view, int position);
        void onResidentSwitch(View view, int position, boolean resident);
    }

    private void setOnLocationItemClickListener(OnLocationItemClickListener l){
        this.listener = l;
    }

    // I custom adapter.

    @Override
    public String getCustomStringForElement(int element) {
        if (itemList == null || itemList.size() == 0) {
            return "";
        }
        return itemList.get(element).getWeatherSource().getSourceUrl();
    }
}

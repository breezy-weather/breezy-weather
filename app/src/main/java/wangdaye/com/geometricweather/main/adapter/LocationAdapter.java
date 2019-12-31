package wangdaye.com.geometricweather.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.text.DateFormat;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Location adapter.
 * */

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder>
        implements ICustomAdapter {

    private GeoActivity activity;
    private OnLocationItemClickListener listener = null;

    public List<Location> itemList;

    private @Nullable MainThemePicker themePicker;
    private @NonNull ResourceProvider resourceProvider;
    private @NonNull WeatherSource defaultSource;
    private @NonNull TemperatureUnit temperatureUnit;

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout locationItemContainer;
        AppCompatImageView swipeIconStart;
        AppCompatImageView swipeIconEnd;

        LinearLayout locationItemView;
        ImageView weatherIcon;
        AppCompatImageView residentIcon;
        TextView title;
        TextView alerts;

        TextView subtitle;
        TextView source;

        private int direction;
        private @ColorInt int swipeRightColor;

        ViewHolder(View itemView, OnLocationItemClickListener listener) {
            super(itemView);

            this.locationItemContainer = itemView.findViewById(R.id.item_location_container);
            this.swipeIconStart = itemView.findViewById(R.id.item_location_swipeIcon_start);
            this.swipeIconEnd = itemView.findViewById(R.id.item_location_swipeIcon_end);

            this.locationItemView = itemView.findViewById(R.id.item_location_item);
            this.weatherIcon = itemView.findViewById(R.id.item_location_weather_icon);
            this.residentIcon = itemView.findViewById(R.id.item_location_resident_icon);
            this.title = itemView.findViewById(R.id.item_location_title);
            this.alerts = itemView.findViewById(R.id.item_location_alerts);

            this.subtitle = itemView.findViewById(R.id.item_location_subtitle);
            this.source = itemView.findViewById(R.id.item_location_source);

            locationItemContainer.setOnClickListener(v -> listener.onClick(v, getAdapterPosition()));
        }

        @SuppressLint("SetTextI18n")
        void onBindView(Location location) {
            direction = 0;
            swipeRightColor = ContextCompat.getColor(activity, location.isCurrentPosition()
                    ? R.color.colorPrimary : R.color.colorTextAlert);

            // icon.
            if (location.isCurrentPosition()) {
                swipeIconEnd.setImageResource(R.drawable.ic_settings);
            } else {
                swipeIconEnd.setImageResource(
                        location.isResidentPosition()
                                ? R.drawable.ic_tag_off
                                : R.drawable.ic_tag_plus
                );
            }

            // background.
            if (themePicker != null) {
                locationItemView.setBackgroundColor(themePicker.getRootColor(activity));
            } else {
                locationItemView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorRoot));
            }

            residentIcon.setVisibility(location.isResidentPosition() ? View.VISIBLE : View.GONE);

            // title.
            if (themePicker != null) {
                title.setTextColor(themePicker.getTextTitleColor(activity));
            } else {
                title.setTextColor(ContextCompat.getColor(activity, R.color.colorTextTitle));
            }
            StringBuilder builder = new StringBuilder(location.isCurrentPosition()
                    ? activity.getString(R.string.current_location)
                    : location.getCityName(activity));
            if (location.getWeather() != null) {
                builder.append(", ").append(
                        location.getWeather().getCurrent().getTemperature().getTemperature(temperatureUnit)
                );
            }
            title.setText(builder.toString());

            // weather.
            if (themePicker != null) {
                alerts.setTextColor(themePicker.getTextSubtitleColor(activity));
            } else {
                alerts.setTextColor(ContextCompat.getColor(activity, R.color.colorTextSubtitle));
            }
            if (location.getWeather() != null) {
                weatherIcon.setVisibility(View.VISIBLE);


                weatherIcon.setImageDrawable(
                        resourceProvider.getWeatherIcon(
                                location.getWeather().getCurrent().getWeatherCode(),
                                TimeManager.isDaylight(location)
                        )
                );

                List<Alert> alertList = location.getWeather().getAlertList();
                if (alertList.size() > 0) {
                    alerts.setVisibility(View.VISIBLE);

                    builder = new StringBuilder();
                    for (int i = 0; i < alertList.size(); i ++) {
                        builder.append(alertList.get(i).getDescription())
                                .append(", ")
                                .append(
                                        DateFormat.getDateTimeInstance(
                                                DateFormat.SHORT,
                                                DateFormat.SHORT
                                        ).format(alertList.get(i).getDate())
                                );
                        if (i != alertList.size() - 1) {
                            builder.append("\n");
                        }
                    }
                    alerts.setText(builder.toString());
                } else {
                    alerts.setVisibility(View.GONE);
                }
            } else {
                weatherIcon.setVisibility(View.GONE);
                alerts.setVisibility(View.GONE);
            }

            // subtitle.
            if (themePicker != null) {
                subtitle.setTextColor(themePicker.getTextContentColor(activity));
            } else {
                subtitle.setTextColor(ContextCompat.getColor(activity, R.color.colorTextContent));
            }
            if (!location.isCurrentPosition() || location.isUsable()) {
                builder = new StringBuilder(
                        location.getCountry() + " " + location.getProvince()
                );
                if (!location.getProvince().equals(location.getCity())) {
                    builder.append(" ").append(location.getCity());
                }
                if (!location.getCity().equals(location.getDistrict())) {
                    builder.append(" ").append(location.getDistrict());
                }
                subtitle.setText(builder.toString());
            } else {
                subtitle.setText(activity.getString(R.string.feedback_not_yet_location));
            }

            // source.
            WeatherSource weatherSource = location.isCurrentPosition()
                    ? defaultSource
                    : location.getWeatherSource();
            source.setText("Powered by " + weatherSource.getSourceUrl());
            source.setTextColor(weatherSource.getSourceColor());

            drawSwipe(0);
            drawDrag(activity, false);
        }

        public ViewHolder drawDrag(Context context, boolean elevate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                locationItemContainer.setElevation(DisplayUtils.dpToPx(context, elevate ? 10 : 0));
            }
            return this;
        }

        public ViewHolder drawSwipe(float dX) {
            if (itemView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                if (dX < 0 && direction >= 0) {
                    direction = -1;
                    locationItemContainer.setBackgroundColor(ContextCompat.getColor(activity, R.color.striking_red));
                } else if (dX > 0 && direction <= 0) {
                    direction = 1;
                    locationItemContainer.setBackgroundColor(swipeRightColor);
                }

                locationItemContainer.setTranslationX(0);
                locationItemView.setTranslationX(dX);
                swipeIconStart.setTranslationX((float) Math.max(0.5 * (dX + swipeIconEnd.getMeasuredWidth()), 0));
                swipeIconEnd.setTranslationX((float) Math.min(0.5 * (dX - swipeIconStart.getMeasuredWidth()), 0));
            } else {
                if (dX < 0 && direction >= 0) {
                    direction = -1;
                    locationItemContainer.setBackgroundColor(swipeRightColor);
                } else if (dX > 0 && direction <= 0) {
                    direction = 1;
                    locationItemContainer.setBackgroundColor(ContextCompat.getColor(activity, R.color.striking_red));
                }

                locationItemContainer.setTranslationX(0);
                locationItemView.setTranslationX(dX);
                swipeIconStart.setTranslationX((float) Math.min(0.5 * (dX - swipeIconStart.getMeasuredWidth()), 0));
                swipeIconEnd.setTranslationX((float) Math.max(0.5 * (dX + swipeIconEnd.getMeasuredWidth()), 0));
            }

            return this;
        }
    }

    public LocationAdapter(GeoActivity activity, List<Location> itemList,
                           @Nullable MainThemePicker themePicker, OnLocationItemClickListener l) {
        this.activity = activity;
        this.itemList = itemList;
        this.resourceProvider = ResourcesProviderFactory.getNewInstance();
        this.themePicker = themePicker;
        this.defaultSource = SettingsOptionManager.getInstance(activity).getWeatherSource();
        this.temperatureUnit = SettingsOptionManager.getInstance(activity).getTemperatureUnit();
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
        holder.onBindView(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void changeData(List<Location> newList, @Nullable MainThemePicker picker) {
        boolean forceUpdate;

        if (themePicker == null && picker == null) {
            forceUpdate = false;
        } else if (themePicker != null && picker != null) {
            forceUpdate = themePicker.isLightTheme() != picker.isLightTheme();
        } else {
            forceUpdate = true;
        }

        WeatherSource newSource = SettingsOptionManager.getInstance(activity).getWeatherSource();
        forceUpdate |= defaultSource != newSource;

        themePicker = picker;
        defaultSource = newSource;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new DiffCallback(itemList, newList, forceUpdate), false);
        itemList.clear();
        itemList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void insertData(Location item) {
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
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

class DiffCallback extends DiffUtil.Callback {

    private List<Location> oldList;
    private List<Location> newList;
    private boolean forceUpdate;

    DiffCallback(List<Location> oldList, List<Location> newList, boolean forceUpdate) {
        this.oldList = oldList;
        this.newList = newList;
        this.forceUpdate = forceUpdate;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (forceUpdate) {
            return false;
        }

        if (!oldList.get(oldItemPosition).toString().equals(newList.get(newItemPosition).toString())) {
            return false;
        }

        if ((oldList.get(oldItemPosition).getWeather() == null && newList.get(newItemPosition).getWeather() != null)
                || (oldList.get(oldItemPosition).getWeather() != null && newList.get(newItemPosition).getWeather() == null)) {
            return false;
        } else if (oldList.get(oldItemPosition).getWeather() != null && newList.get(newItemPosition).getWeather() != null) {
            return oldList.get(oldItemPosition).getWeather().getBase().getTimeStamp()
                    == newList.get(newItemPosition).getWeather().getBase().getTimeStamp();
        }

        return oldList.get(oldItemPosition).isResidentPosition() == newList.get(newItemPosition).isResidentPosition();
    }
}
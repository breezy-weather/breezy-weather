package wangdaye.com.geometricweather.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
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
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

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

    private @Nullable MainThemePicker themePicker;
    private @NonNull ResourceProvider resourceProvider;
    private @NonNull WeatherSource defaultSource;
    private @NonNull TemperatureUnit temperatureUnit;

    private boolean tablet;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        RelativeLayout locationItemContainer;
        LinearLayout locationItemView;
        AppCompatImageView deleteImageLeft;
        AppCompatImageView deleteImageRight;
        TextView title;
        TextView subtitle;
        TextView source;
        AppCompatImageView dragIcon;
        AppCompatImageButton settingsButton;
        AppCompatImageButton deleteButton;

        LinearLayout weatherContainer;
        ImageView weatherIcon;
        TextView weatherText;
        TextView alerts;

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
            this.dragIcon = itemView.findViewById(R.id.item_location_dragIcon);
            this.settingsButton = itemView.findViewById(R.id.item_location_settingsBtn);
            this.deleteButton = itemView.findViewById(R.id.item_location_deleteBtn);

            this.weatherContainer = itemView.findViewById(R.id.item_location_weather_container);
            this.weatherIcon = itemView.findViewById(R.id.item_location_weather_icon);
            this.weatherText = itemView.findViewById(R.id.item_location_weather_text);
            this.alerts = itemView.findViewById(R.id.item_location_alerts);

            this.listener = listener;

            locationItemContainer.setOnClickListener(this);
            settingsButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        void onBindView(Location location, boolean manage) {            
            // background.
            if (themePicker != null) {
                locationItemView.setBackgroundColor(themePicker.getRootColor(activity));
            } else {
                locationItemView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorRoot));
            }
            
            // title.
            if (themePicker != null) {
                title.setTextColor(themePicker.getTextTitleColor(activity));
            } else {
                title.setTextColor(ContextCompat.getColor(activity, R.color.colorTextTitle));
            }
            if (location.isCurrentPosition()) {
                title.setText(activity.getString(R.string.current_location));
            } else {
                title.setText(location.getCityName(activity));
            }

            // subtitle.
            if (themePicker != null) {
                subtitle.setTextColor(themePicker.getTextSubtitleColor(activity));
            } else {
                subtitle.setTextColor(ContextCompat.getColor(activity, R.color.colorTextSubtitle));
            }
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
            
            // icon.
            if (themePicker != null) {
                int contentColor = themePicker.getTextContentColor(activity);
                dragIcon.setSupportImageTintList(ColorStateList.valueOf(contentColor));
                deleteButton.setSupportImageTintList(ColorStateList.valueOf(contentColor));
            } else {
                ColorStateList colorStateList = ColorStateList.valueOf(
                        ContextCompat.getColor(activity, R.color.colorTextContent));
                dragIcon.setSupportImageTintList(colorStateList);
                settingsButton.setSupportImageTintList(colorStateList);
                deleteButton.setSupportImageTintList(colorStateList);
            }
            if (manage) {
                dragIcon.setVisibility(View.VISIBLE);
                settingsButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                setSettingsButton(location);
            } else {
                dragIcon.setVisibility(View.GONE);
                settingsButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            }

            // weather.
            if (themePicker != null) {
                weatherText.setTextColor(themePicker.getTextTitleColor(activity));
                alerts.setTextColor(themePicker.getTextSubtitleColor(activity));
            } else {
                weatherText.setTextColor(ContextCompat.getColor(activity, R.color.colorTextTitle));
                alerts.setTextColor(ContextCompat.getColor(activity, R.color.colorTextSubtitle));
            }

            if (tablet && location.getWeather() != null) {
                weatherContainer.setVisibility(View.VISIBLE);
                alerts.setVisibility(View.VISIBLE);

                weatherIcon.setImageDrawable(
                        resourceProvider.getWeatherIcon(
                                location.getWeather().getCurrent().getWeatherCode(),
                                TimeManager.isDaylight(location)
                        )
                );

                Daily daily = location.getWeather().getDailyForecast().get(0);
                weatherText.setText(
                        daily.day().getTemperature().getShortTemperature(temperatureUnit)
                                + "/"
                                + daily.night().getTemperature().getShortTemperature(temperatureUnit)
                );

                List<Alert> alertList = location.getWeather().getAlertList();
                StringBuilder builder = new StringBuilder();
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
                weatherContainer.setVisibility(View.GONE);
                alerts.setVisibility(View.GONE);
            }

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
            locationItemContainer.setTranslationX(0);
            locationItemView.setTranslationX(dX);
            deleteImageLeft.setTranslationX((float) Math.min(0.5 * (dX - deleteImageLeft.getMeasuredWidth()), 0));
            deleteImageRight.setTranslationX((float) Math.max(0.5 * (dX + deleteImageRight.getMeasuredWidth()), 0));

            return this;
        }

        void setSettingsButton(Location location) {
            if (themePicker != null) {
                settingsButton.setSupportImageTintList(ColorStateList.valueOf(
                        themePicker.getTextContentColor(activity)));
            } else {
                settingsButton.setSupportImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(activity, R.color.colorTextContent)));
            }
            if (location.isCurrentPosition()) {
                settingsButton.setImageResource(R.drawable.ic_settings);
            } else {
                settingsButton.setImageResource(
                        location.isResidentPosition()
                                ? R.drawable.ic_star
                                : R.drawable.ic_star_outline
                );
                if (location.isResidentPosition()) {
                    settingsButton.setSupportImageTintList(
                            ColorStateList.valueOf(
                                    ContextCompat.getColor(activity, R.color.colorTextAlert)
                            )
                    );
                }
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
                           @Nullable MainThemePicker themePicker, boolean manage, OnLocationItemClickListener l) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.itemList = itemList;
        this.resourceProvider = ResourcesProviderFactory.getNewInstance();
        this.themePicker = themePicker;
        this.defaultSource = SettingsOptionManager.getInstance(activity).getWeatherSource();
        this.temperatureUnit = SettingsOptionManager.getInstance(activity).getTemperatureUnit();
        this.manage = manage;
        this.tablet = DisplayUtils.isTabletDevice(activity);
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
        holder.onBindView(itemList.get(position), manage);
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
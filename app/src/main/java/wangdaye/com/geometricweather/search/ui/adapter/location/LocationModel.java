package wangdaye.com.geometricweather.search.ui.adapter.location;

import android.content.Context;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class LocationModel {

    public @NonNull Location location;

    public @NonNull WeatherSource weatherSource;

    public @NonNull String title;
    public @NonNull String subtitle;

    public LocationModel(
            @NonNull Context context,
            @NonNull Location location
    ) {
        this.location = location;

        this.weatherSource = location.isCurrentPosition()
                ? SettingsManager.getInstance(context).getWeatherSource()
                : location.getWeatherSource();

        title = location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.getCityName(context);

        if (!location.isCurrentPosition() || location.isUsable()) {
            subtitle = location.toString();
        } else {
            subtitle = context.getString(R.string.feedback_not_yet_location);
        }
    }

    public boolean areItemsTheSame(@NonNull LocationModel newItem) {
        return location.getFormattedId().equals(
                newItem.location.getFormattedId()
        );
    }

    public boolean areContentsTheSame(@NonNull LocationModel newItem) {
        return location.equals(newItem.location);
    }
}

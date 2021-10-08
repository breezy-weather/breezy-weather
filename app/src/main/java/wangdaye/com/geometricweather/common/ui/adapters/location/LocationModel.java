package wangdaye.com.geometricweather.common.ui.adapters.location;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class LocationModel {

    public @NonNull Location location;

    public @Nullable WeatherCode weatherCode;
    public @NonNull WeatherSource weatherSource;
    public boolean currentPosition;
    public boolean residentPosition;

    public @NonNull String title;
    public @NonNull String subtitle;

    // public float latitude;
    // public float longitude;
    // public TimeZone timeZone;

    public @Nullable String alerts;

    public boolean lightTheme;
    public boolean selected;

    private final boolean mForceUpdate;

    public LocationModel(@NonNull Context context,
                         @NonNull Location location,
                         @NonNull TemperatureUnit unit,
                         boolean lightTheme, boolean selected, boolean forceUpdate) {
        this.location = location;

        if (location.getWeather() != null) {
            this.weatherCode = location.isDaylight()
                    ? location.getWeather().getDailyForecast().get(0).day().getWeatherCode()
                    : location.getWeather().getDailyForecast().get(0).night().getWeatherCode();
        } else {
            this.weatherCode = null;
        }

        this.weatherSource = location.isCurrentPosition()
                ? SettingsManager.getInstance(context).getWeatherSource()
                : location.getWeatherSource();

        this.currentPosition = location.isCurrentPosition();
        this.residentPosition = location.isResidentPosition();

        StringBuilder builder = new StringBuilder(location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.getCityName(context));
        if (location.getWeather() != null) {
            builder.append(", ").append(
                    Temperature.getTrendTemperature(
                            context,
                            location.getWeather().getDailyForecast().get(0).night().getTemperature().getTemperature(),
                            location.getWeather().getDailyForecast().get(0).day().getTemperature().getTemperature(),
                            unit
                    )
            );
        }
        title = builder.toString();

        if (!location.isCurrentPosition() || location.isUsable()) {
            subtitle = location.toString();
        } else {
            subtitle = context.getString(R.string.feedback_not_yet_location);
        }

        // latitude = location.getLatitude();
        // longitude = location.getLongitude();
        // timeZone = location.getTimeZone();

        if (location.getWeather() != null) {
            List<Alert> alertList = location.getWeather().getAlertList();
            if (alertList.size() > 0) {
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
                alerts = builder.toString();
            } else if (!TextUtils.isEmpty(location.getWeather().getCurrent().getDailyForecast())) {
                alerts = location.getWeather().getCurrent().getDailyForecast();
            } else if (!TextUtils.isEmpty(location.getWeather().getCurrent().getHourlyForecast())) {
                alerts = location.getWeather().getCurrent().getHourlyForecast();
            } else {
                alerts = null;
            }
        } else {
            alerts = null;
        }

        this.lightTheme = lightTheme;
        this.selected = selected;

        mForceUpdate = forceUpdate;
    }

    public boolean areItemsTheSame(@NonNull LocationModel newItem) {
        return location.getFormattedId().equals(
                newItem.location.getFormattedId()
        );
    }

    public boolean areContentsTheSame(@NonNull LocationModel newItem) {
        return weatherCode == newItem.weatherCode
                && weatherSource == newItem.weatherSource
                && currentPosition == newItem.currentPosition
                && residentPosition == newItem.residentPosition
                && isSameString(title, newItem.title)
                && isSameString(subtitle, newItem.subtitle)
                && isSameString(alerts, newItem.alerts)
                // && latitude == newItem.latitude
                // && longitude == newItem.longitude
                // && timeZone.getID().equals(newItem.timeZone.getID())
                && lightTheme == newItem.lightTheme
                && selected == newItem.selected
                && !newItem.mForceUpdate;
    }

    private static boolean isSameString(String a, String b) {
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        } else {
            return TextUtils.isEmpty(a) && TextUtils.isEmpty(b);
        }
    }
}

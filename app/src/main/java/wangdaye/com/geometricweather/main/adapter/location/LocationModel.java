package wangdaye.com.geometricweather.main.adapter.location;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;

public class LocationModel {

    public @NonNull Location location;

    public @Nullable WeatherCode weatherCode;
    public @NonNull WeatherSource weatherSource;
    public boolean currentPosition;
    public boolean residentPosition;

    public @NonNull String title;
    public @NonNull String subtitle;
    public @Nullable String alerts;

    private boolean lightTheme;
    private boolean forceUpdate;

    public LocationModel(Context context, Location location, TemperatureUnit unit, WeatherSource defaultSource,
                         boolean lightTheme, boolean forceUpdate) {
        this.location = location;

        if (location.getWeather() != null) {
            this.weatherCode = location.getWeather().getCurrent().getWeatherCode();
        } else {
            this.weatherCode = null;
        }

        this.weatherSource = location.isCurrentPosition()
                ? defaultSource
                : location.getWeatherSource();

        this.currentPosition = location.isCurrentPosition();
        this.residentPosition = location.isResidentPosition();

        StringBuilder builder = new StringBuilder(location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.getCityName(context));
        if (location.getWeather() != null) {
            builder.append(", ").append(
                    location.getWeather().getCurrent().getTemperature().getTemperature(unit)
            );
        }
        title = builder.toString();

        if (!location.isCurrentPosition() || location.isUsable()) {
            builder = new StringBuilder(location.getCountry() + " " + location.getProvince());
            if (!location.getProvince().equals(location.getCity())
                    && !TextUtils.isEmpty(location.getCity())) {
                builder.append(" ").append(location.getCity());
            }
            if (!location.getCity().equals(location.getDistrict())
                    && !TextUtils.isEmpty(location.getDistrict())) {
                builder.append(" ").append(location.getDistrict());
            }
            subtitle = builder.toString();
        } else {
            subtitle = context.getString(R.string.feedback_not_yet_location);
        }

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
        this.forceUpdate = forceUpdate;
    }

    public boolean areItemsTheSame(@NonNull LocationModel newItem) {
        return location.equals(newItem.location);
    }

    public boolean areContentsTheSame(@NonNull LocationModel newItem) {
        return weatherCode == newItem.weatherCode
                && weatherSource == newItem.weatherSource
                && currentPosition == newItem.currentPosition
                && residentPosition == newItem.residentPosition
                && isSameString(title, newItem.title)
                && isSameString(subtitle, newItem.subtitle)
                && isSameString(alerts, newItem.alerts)
                && lightTheme == newItem.lightTheme
                && !newItem.forceUpdate;
    }

    private static boolean isSameString(String a, String b) {
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        } else {
            return TextUtils.isEmpty(a) && TextUtils.isEmpty(b);
        }
    }
}

package wangdaye.com.geometricweather.main.adapters.location;

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
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;

public class LocationModel {

    public @NonNull Location location;

    public @Nullable WeatherCode weatherCode;
    public @NonNull WeatherSource weatherSource;
    public boolean currentPosition;
    public boolean residentPosition;

    public @NonNull String title1;
    public @NonNull String title2;
    public @NonNull String subtitle;

    public @Nullable String alerts;

    public boolean selected;

    public LocationModel(
            @NonNull Context context,
            @NonNull Location location,
            @NonNull TemperatureUnit unit,
            boolean selected
    ) {
        this.location = location;

        this.weatherCode = null;
        if (location.getWeather() != null && location.getWeather().getDailyForecast().size() > 0) {
            if (location.isDaylight()
                    && location.getWeather().getDailyForecast().get(0).getDay() != null
                    && location.getWeather().getDailyForecast().get(0).getDay().getWeatherCode() != null
            ) {
                this.weatherCode = location.getWeather().getDailyForecast().get(0).getDay().getWeatherCode();
            }
            if (!location.isDaylight()
                    && location.getWeather().getDailyForecast().get(0).getNight() != null
                    && location.getWeather().getDailyForecast().get(0).getNight().getWeatherCode() != null
            ) {
                this.weatherCode = location.getWeather().getDailyForecast().get(0).getNight().getWeatherCode();
            }
        }

        this.weatherSource = location.getWeatherSource();

        this.currentPosition = location.isCurrentPosition();
        this.residentPosition = location.isResidentPosition();

        this.title1 = location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.getCityName(context);
        this.title2 = "";
        if (location.getWeather() != null && location.getWeather().getCurrent() != null) {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(location.getWeather().getCurrent().getWeatherText())) {
                builder.append(location.getWeather().getCurrent().getWeatherText());
                if (location.getWeather().getCurrent().getTemperature() != null
                    && location.getWeather().getCurrent().getTemperature().getTemperature() != null) {
                    builder.append(", ");
                }
            }
            if (location.getWeather().getCurrent().getTemperature() != null
                    && location.getWeather().getCurrent().getTemperature().getTemperature() != null) {
                builder.append(unit
                        .getShortValueText(
                                context,
                                location.getWeather().getCurrent().getTemperature().getTemperature()
                        )
                );
            }
            this.title2 = builder.toString();
        }

        if (!location.isCurrentPosition() || location.isUsable()) {
            subtitle = location.toString();
        } else {
            subtitle = context.getString(R.string.feedback_not_yet_location);
        }

        if (location.getWeather() != null) {
            List<Alert> alertList = location.getWeather().getAlertList();
            if (alertList.size() > 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < alertList.size(); i++) {
                    if (alertList.get(i).getDate() != null) {
                        builder.append(alertList.get(i).getDescription())
                                .append(", ")
                                .append(
                                        DateFormat.getDateTimeInstance(
                                                DateFormat.SHORT,
                                                DateFormat.SHORT
                                        ).format(alertList.get(i).getDate())
                                );
                    }
                    if (i != alertList.size() - 1) {
                        builder.append("\n");
                    }
                }
                alerts = builder.toString();
            } else {
                alerts = null;
            }
        } else {
            alerts = null;
        }

        this.selected = selected;
    }

    public boolean areItemsTheSame(@NonNull LocationModel newItem) {
        return location.getFormattedId().equals(
                newItem.location.getFormattedId()
        );
    }

    public boolean areContentsTheSame(@NonNull LocationModel newItem) {
        return location.equals(newItem.location) && selected == newItem.selected;
    }
}

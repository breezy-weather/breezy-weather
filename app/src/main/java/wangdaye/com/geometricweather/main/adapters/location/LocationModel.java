package wangdaye.com.geometricweather.main.adapters.location;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class LocationModel {

    public @NonNull Location location;

    public @Nullable WeatherCode weatherCode;
    public @Nullable String weatherText;
    public @NonNull WeatherSource weatherSource;
    public boolean currentPosition;
    public boolean residentPosition;

    public @NonNull String title;
    public @NonNull String body;

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
        // TODO: Use current instead
        if (location.getWeather() != null && location.getWeather().getDailyForecast().size() > 0) {
            if (location.isDaylight()
                    && location.getWeather().getDailyForecast().get(0).getDay() != null
                    && location.getWeather().getDailyForecast().get(0).getDay().getWeatherCode() != null
            ) {
                this.weatherCode = location.getWeather().getDailyForecast().get(0).getDay().getWeatherCode();
                this.weatherText = location.getWeather().getDailyForecast().get(0).getDay().getWeatherText();
            }
            if (!location.isDaylight()
                    && location.getWeather().getDailyForecast().get(0).getNight() != null
                    && location.getWeather().getDailyForecast().get(0).getNight().getWeatherCode() != null
            ) {
                this.weatherCode = location.getWeather().getDailyForecast().get(0).getNight().getWeatherCode();
                this.weatherText = location.getWeather().getDailyForecast().get(0).getNight().getWeatherText();
            }
        }

        this.weatherSource = location.getWeatherSource();

        this.currentPosition = location.isCurrentPosition();
        this.residentPosition = location.isResidentPosition();

        this.title = location.isCurrentPosition()
                ? context.getString(R.string.current_location)
                : location.place();
        this.body = (location.isUsable()) ? location.administrationLevels() : context.getString(R.string.feedback_not_yet_location);

        if (location.getWeather() != null) {
            List<Alert> alertList = location.getWeather().getCurrentAlertList();
            if (alertList.size() > 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < alertList.size(); i++) {
                    builder.append(alertList.get(i).getDescription());
                    if (alertList.get(i).getStartDate() != null) {
                        String startDateDay = DisplayUtils.getFormattedDate(alertList.get(i).getStartDate(),
                                location.getTimeZone(),
                                context.getString(R.string.date_format_short));
                        builder.append(", ")
                            .append(startDateDay)
                            .append(", ")
                            .append(DisplayUtils.getFormattedDate(alertList.get(i).getStartDate(),
                                    location.getTimeZone(),
                                    (DisplayUtils.is12Hour(context)) ? "h:mm aa" : "HH:mm"));
                        if (alertList.get(i).getEndDate() != null) {
                            builder.append("-");
                            String endDateDay = DisplayUtils.getFormattedDate(alertList.get(i).getEndDate(),
                                    location.getTimeZone(),
                                    context.getString(R.string.date_format_short));
                            if (!startDateDay.equals(endDateDay)) {
                                builder.append(endDateDay)
                                        .append(", ");
                            }
                            builder.append(DisplayUtils.getFormattedDate(alertList.get(i).getEndDate(),
                                    location.getTimeZone(),
                                    (DisplayUtils.is12Hour(context)) ? "h:mm aa" : "HH:mm"));
                        }
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

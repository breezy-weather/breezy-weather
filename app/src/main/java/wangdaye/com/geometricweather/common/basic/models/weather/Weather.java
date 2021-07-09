package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

public class Weather
        implements Serializable {

    @NonNull private final Base base;
    @NonNull private final Current current;
    @Nullable private History yesterday;
    @NonNull private final List<Daily> dailyForecast;
    @NonNull private final List<Hourly> hourlyForecast;
    @NonNull private final List<Minutely> minutelyForecast;
    @NonNull private final List<Alert> alertList;

    public Weather(@NonNull Base base, @NonNull Current current, @Nullable History yesterday,
                   @NonNull List<Daily> dailyForecast,
                   @NonNull List<Hourly> hourlyForecast,
                   @NonNull List<Minutely> minutelyForecast,
                   @NonNull List<Alert> alertList) {
        this.base = base;
        this.current = current;
        this.yesterday = yesterday;
        this.dailyForecast = dailyForecast;
        this.hourlyForecast = hourlyForecast;
        this.minutelyForecast = minutelyForecast;
        this.alertList = alertList;
    }

    @NonNull
    public Base getBase() {
        return base;
    }

    @NonNull
    public Current getCurrent() {
        return current;
    }

    public void setYesterday(@Nullable History yesterday) {
        this.yesterday = yesterday;
    }

    @Nullable
    public History getYesterday() {
        return yesterday;
    }

    @NonNull
    public List<Daily> getDailyForecast() {
        return dailyForecast;
    }

    @NonNull
    public List<Hourly> getHourlyForecast() {
        return hourlyForecast;
    }

    @NonNull
    public List<Minutely> getMinutelyForecast() {
        return minutelyForecast;
    }

    @NonNull
    public List<Alert> getAlertList() {
        return alertList;
    }

    public boolean isValid(float pollingIntervalHours) {
        long updateTime = base.getUpdateTime();
        long currentTime = System.currentTimeMillis();
        return currentTime >= updateTime
                && currentTime - updateTime < pollingIntervalHours * 60 * 60 * 1000;
    }

    public boolean isDaylight(TimeZone timeZone) {
        Date riseDate = getDailyForecast().get(0).sun().getRiseDate();
        Date setDate = getDailyForecast().get(0).sun().getSetDate();
        if (riseDate != null && setDate != null) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(timeZone);
            int time = 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);

            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTime(riseDate);
            int sunrise = 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);
            calendar.setTime(setDate);
            int sunset = 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);

            return sunrise < time && time < sunset;
        }

        return DisplayUtils.isDaylight(timeZone);
    }
}

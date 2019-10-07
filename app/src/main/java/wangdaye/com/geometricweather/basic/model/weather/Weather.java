package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class Weather {

    @NonNull private Base base;
    @NonNull private Current current;
    @Nullable private History yesterday;
    @NonNull private List<Daily> dailyForecast;
    @NonNull private List<Hourly> hourlyForecast;
    @NonNull private List<Minutely> minutelyForecast;
    @NonNull private List<Alert> alertList;

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

    public boolean isValid(float hour) {
        long updateTime = base.getUpdateTime();
        long currentTime = System.currentTimeMillis();
        return currentTime >= updateTime
                && currentTime - updateTime < hour * 60 * 60 * 1000;
    }
}

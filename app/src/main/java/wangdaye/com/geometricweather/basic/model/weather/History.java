package wangdaye.com.geometricweather.basic.model.weather;

import java.util.Date;

/**
 * History.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class History {

    private Date date;
    private long time;

    private int daytimeTemperature;
    private int nighttimeTemperature;

    public History(Date date, long time, int daytimeTemperature, int nighttimeTemperature) {
        this.date = date;
        this.time = time;
        this.daytimeTemperature = daytimeTemperature;
        this.nighttimeTemperature = nighttimeTemperature;
    }

    public Date getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public int getDaytimeTemperature() {
        return daytimeTemperature;
    }

    public int getNighttimeTemperature() {
        return nighttimeTemperature;
    }
}

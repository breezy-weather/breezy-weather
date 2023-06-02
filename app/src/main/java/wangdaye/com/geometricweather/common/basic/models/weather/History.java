package wangdaye.com.geometricweather.common.basic.models.weather;

import java.io.Serializable;
import java.util.Date;

/**
 * History.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class History implements Serializable {

    private final Date date;
    private final long time;

    private final Integer daytimeTemperature;
    private final Integer nighttimeTemperature;

    public History(Date date, long time, Integer daytimeTemperature, Integer nighttimeTemperature) {
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

    public Integer getDaytimeTemperature() {
        return daytimeTemperature;
    }

    public Integer getNighttimeTemperature() {
        return nighttimeTemperature;
    }
}

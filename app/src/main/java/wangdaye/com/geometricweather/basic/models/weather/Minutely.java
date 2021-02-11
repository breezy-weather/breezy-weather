package wangdaye.com.geometricweather.basic.models.weather;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

/**
 * Minutely.
 * */
public class Minutely implements Serializable {

    private final Date date;
    private final long time;
    private final boolean daylight;

    private final String weatherText;
    private final WeatherCode weatherCode;

    private final int minuteInterval;
    @Nullable private final Integer dbz;
    @Nullable private final Integer cloudCover;

    public Minutely(Date date, long time, boolean daylight,
                    String weatherText, WeatherCode weatherCode,
                    int minuteInterval, @Nullable Integer dbz, @Nullable Integer cloudCover) {
        this.date = date;
        this.time = time;
        this.daylight = daylight;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.minuteInterval = minuteInterval;
        this.dbz = dbz;
        this.cloudCover = cloudCover;
    }

    public Date getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public boolean isDaylight() {
        return daylight;
    }

    public String getWeatherText() {
        return weatherText;
    }

    public WeatherCode getWeatherCode() {
        return weatherCode;
    }

    public int getMinuteInterval() {
        return minuteInterval;
    }

    @Nullable
    public Integer getDbz() {
        return dbz;
    }

    @Nullable
    public Integer getCloudCover() {
        return cloudCover;
    }

    public boolean isPrecipitation() {
        return weatherCode == WeatherCode.RAIN
                || weatherCode == WeatherCode.SNOW
                || weatherCode == WeatherCode.SLEET
                || weatherCode == WeatherCode.HAIL
                || weatherCode == WeatherCode.THUNDERSTORM;
    }
}

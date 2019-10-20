package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.Nullable;

import java.util.Date;

/**
 * Minutely.
 * */
public class Minutely {

    private Date date;
    private long time;
    private boolean daylight;

    private String weatherText;
    private WeatherCode weatherCode;

    private int minuteInterval;
    @Nullable private Integer dbz;
    @Nullable private Integer cloudCover;

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

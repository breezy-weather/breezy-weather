package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

/**
 * Minutely.
 * */
public class Minutely implements Serializable {

    private final Date date;
    private final long time;

    private final String weatherText;
    @Nullable private final WeatherCode weatherCode;

    private final int minuteInterval;
    @Nullable private final Integer dbz;
    @Nullable private final Integer cloudCover;

    public Minutely(Date date, long time,
                    String weatherText, @Nullable WeatherCode weatherCode,
                    int minuteInterval, @Nullable Integer dbz, @Nullable Integer cloudCover) {
        this.date = date;
        this.time = time;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.minuteInterval = minuteInterval;
        this.dbz = dbz;
        this.cloudCover = cloudCover;
    }

    public Minutely(Date date, long time,
                    String weatherText, @Nullable WeatherCode weatherCode,
                    int minuteInterval, @Nullable Double precipitationIntensity, @Nullable Integer cloudCover) {
        this.date = date;
        this.time = time;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.minuteInterval = minuteInterval;
        this.dbz = precipitationIntensityToDBZ(precipitationIntensity);
        this.cloudCover = cloudCover;
    }

    public Date getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public String getWeatherText() {
        return weatherText;
    }

    @Nullable
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
    public Double getPrecipitationIntensity() {
        if (dbz == null) {
            return null;
        }
        if (dbz <= 5) {
            return 0.0;
        }
        return Math.pow(
                Math.pow(10, dbz / 10.0) / 200.0,
                5.0 / 8.0
        );
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

    @Nullable
    private static Integer precipitationIntensityToDBZ(@Nullable Double intensity) {
        if (intensity == null) {
            return null;
        }
        return (int) (
                10.0 * Math.log10(
                        200.0 * Math.pow(intensity, 8.0 / 5.0)
                )
        );
    }
}

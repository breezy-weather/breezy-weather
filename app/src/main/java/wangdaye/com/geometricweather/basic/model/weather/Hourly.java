package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Hourly.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Hourly {

    private Date date;
    private long time;
    private boolean daylight;

    private String weatherText;
    private WeatherCode weatherCode;

    private Temperature temperature;
    private Precipitation precipitation;
    private PrecipitationProbability precipitationProbability;

    public Hourly(Date date, long time, boolean daylight,
                  String weatherText, WeatherCode weatherCode,
                  Temperature temperature,
                  Precipitation precipitation, PrecipitationProbability precipitationProbability) {
        this.date = date;
        this.time = time;
        this.daylight = daylight;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.precipitationProbability = precipitationProbability;
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

    public Temperature getTemperature() {
        return temperature;
    }

    public Precipitation getPrecipitation() {
        return precipitation;
    }

    public PrecipitationProbability getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getHour(Context c) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (TimeManager.is12Hour(c)) {
            int hour = calendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
            return hour + c.getString(R.string.of_clock);
        } else {
            return calendar.get(Calendar.HOUR_OF_DAY) + c.getString(R.string.of_clock);
        }
    }
}

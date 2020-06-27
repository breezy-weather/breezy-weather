package com.mbestavros.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.BidiFormatter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.utils.DisplayUtils;
import com.mbestavros.geometricweather.utils.manager.TimeManager;

/**
 * Hourly.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Hourly implements Serializable {

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

    @SuppressLint("DefaultLocale")
    public String getHour(Context c) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hour;
        if (TimeManager.is12Hour(c)) {
            hour = calendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }

        if (DisplayUtils.isRtl(c)) {
            return BidiFormatter.getInstance().unicodeWrap(String.format("%d", hour))
                    + c.getString(R.string.of_clock);
        } else {
            return hour + c.getString(R.string.of_clock);
        }
    }
}

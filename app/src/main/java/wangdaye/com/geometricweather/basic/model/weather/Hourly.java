package wangdaye.com.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.BidiFormatter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Hourly.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Hourly implements Serializable {

    private final Date date;
    private final long time;
    private final boolean daylight;

    private final String weatherText;
    private final WeatherCode weatherCode;

    private final Temperature temperature;
    private final Precipitation precipitation;
    private final PrecipitationProbability precipitationProbability;

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

    public String getLongDate(Context context) {
        return getDate(context.getString(R.string.date_format_long));
    }

    public String getShortDate(Context context) {
        return getDate(context.getString(R.string.date_format_short));
    }

    @SuppressLint("SimpleDateFormat")
    public String getDate(String format) {
        return new SimpleDateFormat(format).format(date);
    }
}

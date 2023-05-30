package wangdaye.com.geometricweather.common.basic.models.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.BidiFormatter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;

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
    private final Wind wind;
    private final UV uv;

    public Hourly(Date date, long time, boolean daylight,
                  String weatherText, WeatherCode weatherCode,
                  Temperature temperature,
                  Precipitation precipitation, PrecipitationProbability precipitationProbability,
                  Wind wind, UV uv) {
        this.date = date;
        this.time = time;
        this.daylight = daylight;
        this.weatherText = weatherText;
        this.weatherCode = weatherCode;
        this.temperature = temperature;
        this.precipitation = precipitation;
        this.precipitationProbability = precipitationProbability;
        this.wind = wind;
        this.uv = uv;
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

    public Wind getWind() {
        return wind;
    }

    public UV getUV() {
        return uv;
    }

    public int getHourIn24Format(TimeZone timeZone) {
        Calendar calendar = DisplayUtils.toCalendarWithTimeZone(date, timeZone);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public String getHour(Context context, TimeZone timeZone) {
        return getHour(context, timeZone, DisplayUtils.is12Hour(context), DisplayUtils.isRtl(context));
    }

    @SuppressLint("DefaultLocale")
    private String getHour(Context context, TimeZone timeZone, boolean twelveHour, boolean rtl) {
        Calendar calendar = DisplayUtils.toCalendarWithTimeZone(date, timeZone);

        int hour;
        if (twelveHour) {
            hour = calendar.get(Calendar.HOUR);
            if (hour == 0) {
                hour = 12;
            }
        } else {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
        }

        if (rtl) {
            return BidiFormatter.getInstance().unicodeWrap(String.format("%d", hour))
                    + context.getString(R.string.of_clock);
        } else {
            return hour + context.getString(R.string.of_clock);
        }
    }

    public String getLongDate(Context context, TimeZone timeZone) {
        return getDate(timeZone, context.getString(R.string.date_format_long));
    }

    public String getShortDate(Context context, TimeZone timeZone) {
        return getDate(timeZone, context.getString(R.string.date_format_short));
    }

    public String getDate(TimeZone timeZone, String format) {
        return DisplayUtils.getFormattedDate(date, timeZone, format);
    }
}
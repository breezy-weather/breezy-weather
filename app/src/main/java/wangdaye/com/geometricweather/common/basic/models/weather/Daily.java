package wangdaye.com.geometricweather.common.basic.models.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Size;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper;

/**
 * Daily.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Daily implements Serializable {

    private final Date date;
    private final long time;

    @Size(2) private final HalfDay[] halfDays;
    @Size(2) private final Astro[] astros;
    private final MoonPhase moonPhase;
    private final AirQuality airQuality;
    private final Pollen pollen;
    private final UV uv;
    private final float hoursOfSun;

    public Daily(Date date, long time,
                 HalfDay day, HalfDay night, Astro sun, Astro moon,
                 MoonPhase moonPhase, AirQuality airQuality, Pollen pollen, UV uv,
                 float hoursOfSun) {
        this.date = date;
        this.time = time;
        this.halfDays = new HalfDay[] {day, night};
        this.astros = new Astro[] {sun, moon};
        this.moonPhase = moonPhase;
        this.airQuality = airQuality;
        this.pollen = pollen;
        this.uv = uv;
        this.hoursOfSun = hoursOfSun;
    }

    public HalfDay day() {
        return halfDays[0];
    }

    public HalfDay night() {
        return halfDays[1];
    }

    public Astro sun() {
        return astros[0];
    }

    public Astro moon() {
        return astros[1];
    }

    public Date getDate() {
        return date;
    }

    public long getTime() {
        return time;
    }

    public MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public AirQuality getAirQuality() {
        return airQuality;
    }

    public Pollen getPollen() {
        return pollen;
    }

    public UV getUV() {
        return uv;
    }

    public float getHoursOfSun() {
        return hoursOfSun;
    }

    public String getLongDate(Context context, TimeZone timeZone) {
        return getDate(context.getString(R.string.date_format_long), timeZone);
    }

    public String getShortDate(Context context, TimeZone timeZone) {
        return getDate(context.getString(R.string.date_format_short), timeZone);
    }

    public String getDate(String format, TimeZone timeZone) {
        return DisplayUtils.getFormattedDate(date, timeZone, format);
    }

    public String getWeek(Context context, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 1){
            return context.getString(R.string.week_7);
        } else if (day == 2) {
            return context.getString(R.string.week_1);
        } else if (day == 3) {
            return context.getString(R.string.week_2);
        } else if (day == 4) {
            return context.getString(R.string.week_3);
        } else if (day == 5) {
            return context.getString(R.string.week_4);
        } else if (day == 6) {
            return context.getString(R.string.week_5);
        } else {
            return context.getString(R.string.week_6);
        }
    }

    public String getLunar() {
        return LunarHelper.getLunarDate(date);
    }

    public boolean isToday(TimeZone timeZone) {
        Calendar current = Calendar.getInstance(timeZone);
        Calendar thisDay = Calendar.getInstance(timeZone);
        thisDay.setTime(date);

        return current.get(Calendar.YEAR) == thisDay.get(Calendar.YEAR)
                && current.get(Calendar.DAY_OF_YEAR) == thisDay.get(Calendar.DAY_OF_YEAR);
    }
}

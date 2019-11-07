package wangdaye.com.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Size;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;

/**
 * Daily.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class Daily {

    private Date date;
    private long time;

    @Size(2) private HalfDay[] halfDays;
    @Size(2) private Astro[] astros;
    private MoonPhase moonPhase;
    private AirQuality airQuality;
    private Pollen pollen;
    private UV uv;
    private float hoursOfSun;

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

    public String getWeek(Context context) {
        Calendar calendar = Calendar.getInstance();
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
        long millis = System.currentTimeMillis();

        Calendar current = Calendar.getInstance();
        current.add(
                Calendar.MILLISECOND,
                timeZone.getOffset(millis) - TimeZone.getDefault().getOffset(millis)
        );

        Calendar thisDay = Calendar.getInstance();
        thisDay.setTime(date);

        return current.get(Calendar.YEAR) == thisDay.get(Calendar.YEAR)
                && current.get(Calendar.DAY_OF_YEAR) == thisDay.get(Calendar.DAY_OF_YEAR);
    }
}

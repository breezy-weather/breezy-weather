package wangdaye.com.geometricweather.common.basic.models.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import wangdaye.com.geometricweather.common.utils.DisplayUtils;

/**
 * Astro.
 * */
public class Astro implements Serializable {

    @Nullable private final Date riseDate;
    @Nullable private final Date setDate;

    public Astro(@Nullable Date riseDate, @Nullable Date setDate) {
        this.riseDate = riseDate;
        this.setDate = setDate;
    }

    @Nullable
    public Date getRiseDate() {
        return riseDate;
    }

    @Nullable
    public Date getSetDate() {
        return setDate;
    }

    public boolean isValid() {
        return riseDate != null && setDate != null;
    }

    @Nullable
    public String getRiseTime(Context context, TimeZone timeZone) {
        return getRiseTime(DisplayUtils.is12Hour(context), timeZone);
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    private String getRiseTime(boolean twelveHour, TimeZone timeZone) {
        if (riseDate == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(twelveHour ? "h:mm aa" : "HH:mm");
        df.setTimeZone(timeZone);
        return df.format(riseDate);
    }

    @Nullable
    public String getSetTime(Context context, TimeZone timeZone) {
        return getSetTime(DisplayUtils.is12Hour(context), timeZone);
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    private String getSetTime(boolean twelveHour, TimeZone timeZone) {
        if (setDate == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(twelveHour ? "h:mm aa" : "HH:mm");
        df.setTimeZone(timeZone);
        return df.format(setDate);
    }
}

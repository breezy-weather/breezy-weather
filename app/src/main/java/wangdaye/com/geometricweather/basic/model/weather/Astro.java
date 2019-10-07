package wangdaye.com.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Astro.
 * */
public class Astro {

    @Nullable private Date riseDate;
    @Nullable private Date setDate;

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

    @SuppressLint("SimpleDateFormat")
    @Nullable
    public String getRiseTime(Context c) {
        if (riseDate == null) {
            return null;
        }
        if (TimeManager.is12Hour(c)) {
            return new SimpleDateFormat("h:mm aa").format(riseDate);
        } else {
            return new SimpleDateFormat("HH:mm").format(riseDate);
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    public String getSetTime(Context c) {
        if (setDate == null) {
            return null;
        }
        if (TimeManager.is12Hour(c)) {
            return new SimpleDateFormat("h:mm aa").format(setDate);
        } else {
            return new SimpleDateFormat("HH:mm").format(setDate);
        }
    }
}

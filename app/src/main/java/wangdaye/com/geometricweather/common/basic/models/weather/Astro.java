package wangdaye.com.geometricweather.common.basic.models.weather;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import wangdaye.com.geometricweather.common.utils.managers.TimeManager;

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
    public String getRiseTime(Context context) {
        return getRiseTime(TimeManager.is12Hour(context));
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    private String getRiseTime(boolean twelveHour) {
        if (riseDate == null) {
            return null;
        }
        if (twelveHour) {
            return new SimpleDateFormat("h:mm aa").format(riseDate);
        } else {
            return new SimpleDateFormat("HH:mm").format(riseDate);
        }
    }

    @Nullable
    public String getSetTime(Context context) {
        return getSetTime(TimeManager.is12Hour(context));
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    private String getSetTime(boolean twelveHour) {
        if (setDate == null) {
            return null;
        }
        if (twelveHour) {
            return new SimpleDateFormat("h:mm aa").format(setDate);
        } else {
            return new SimpleDateFormat("HH:mm").format(setDate);
        }
    }
}

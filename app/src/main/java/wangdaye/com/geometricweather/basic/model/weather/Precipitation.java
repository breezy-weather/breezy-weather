package wangdaye.com.geometricweather.basic.model.weather;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.R;

/**
 * Precipitation.
 *
 * default unit : {@link wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit#MM}
 * */
public class Precipitation {

    @Nullable private Float total;
    @Nullable private Float thunderstorm;
    @Nullable private Float rain;
    @Nullable private Float snow;
    @Nullable private Float ice;

    public static final float PRECIPITATION_LIGHT = 10;
    public static final float PRECIPITATION_MIDDLE = 25;
    public static final float PRECIPITATION_HEAVY = 50;
    public static final float PRECIPITATION_RAINSTORM = 100;

    public Precipitation(@Nullable Float total,
                         @Nullable Float thunderstorm,
                         @Nullable Float rain,
                         @Nullable Float snow,
                         @Nullable Float ice) {
        this.total = total;
        this.thunderstorm = thunderstorm;
        this.rain = rain;
        this.snow = snow;
        this.ice = ice;
    }

    @Nullable
    public Float getTotal() {
        return total;
    }

    @Nullable
    public Float getThunderstorm() {
        return thunderstorm;
    }

    @Nullable
    public Float getRain() {
        return rain;
    }

    @Nullable
    public Float getSnow() {
        return snow;
    }

    @Nullable
    public Float getIce() {
        return ice;
    }

    public boolean isValid() {
        return total != null && total > 0;
    }

    @ColorInt
    public int getPrecipitationColor(Context context) {
        if (total == null) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (total <= PRECIPITATION_LIGHT) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (total <= PRECIPITATION_MIDDLE) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (total <= PRECIPITATION_HEAVY) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (total <= PRECIPITATION_RAINSTORM) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        }
    }
}

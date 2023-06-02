package wangdaye.com.geometricweather.common.basic.models.weather;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.Serializable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.unit.SpeedUnit;

/**
 * DailyWind.
 *
 * default unit:
 * {@link #speed} : {@link SpeedUnit#KPH}
 * */
public class Wind implements Serializable {

    @Nullable private final String direction;
    @Nullable private final WindDegree degree;
    @Nullable private final Float speed;
    @Nullable private final String level;

    public static final float WIND_SPEED_0 = 2;
    public static final float WIND_SPEED_1 = 6;
    public static final float WIND_SPEED_2 = 12;
    public static final float WIND_SPEED_3 = 19;
    public static final float WIND_SPEED_4 = 30;
    public static final float WIND_SPEED_5 = 40;
    public static final float WIND_SPEED_6 = 51;
    public static final float WIND_SPEED_7 = 62;
    public static final float WIND_SPEED_8 = 75;
    public static final float WIND_SPEED_9 = 87;
    public static final float WIND_SPEED_10 = 103;
    public static final float WIND_SPEED_11 = 117;

    public Wind(@Nullable String direction,
                @Nullable WindDegree degree,
                @Nullable Float speed,
                @Nullable String level) {
        this.direction = direction;
        this.degree = degree;
        this.speed = speed;
        this.level = level;
    }

    @Nullable
    public String getDirection() {
        return direction;
    }

    @Nullable
    public WindDegree getDegree() {
        return degree;
    }

    @Nullable
    public Float getSpeed() {
        return speed;
    }

    @Nullable
    public String getLevel() {
        return level;
    }

    @ColorInt
    public int getWindColor(Context context) {
        if (speed == null) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (speed <= WIND_SPEED_3) {
            return ContextCompat.getColor(context, R.color.colorLevel_1);
        } else if (speed <= WIND_SPEED_5) {
            return ContextCompat.getColor(context, R.color.colorLevel_2);
        } else if (speed <= WIND_SPEED_7) {
            return ContextCompat.getColor(context, R.color.colorLevel_3);
        } else if (speed <= WIND_SPEED_9) {
            return ContextCompat.getColor(context, R.color.colorLevel_4);
        } else if (speed <= WIND_SPEED_11) {
            return ContextCompat.getColor(context, R.color.colorLevel_5);
        } else {
            return ContextCompat.getColor(context, R.color.colorLevel_6);
        }
    }

    public String getShortWindDescription() {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(direction)) {
            builder.append(direction);
            if (!TextUtils.isEmpty(level)) {
                builder.append(" ");
            }
        }
        if (!TextUtils.isEmpty(level)) {
            builder.append(level);
        }
        return builder.toString();
    }

    public String getWindDescription(Context context, SpeedUnit unit) {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(direction)) {
            builder.append(direction);
        }
        if (speed != null) {
            builder.append(" ").append(unit.getValueText(context, speed));
        }
        if (!TextUtils.isEmpty(level)) {
            builder.append(" ").append("(").append(level).append(")");
        }
        if (degree != null && !degree.isNoDirection()) {
            builder.append(" ").append(degree.getWindArrow());
        }
        return builder.toString();
    }

    public boolean isValidSpeed() {
        return speed != null && speed > 0;
    }
}

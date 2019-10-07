package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;

/**
 * Wind.
 *
 * default unit:
 * {@link #speed} : {@link wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit#KPH}
 * */
public class Wind {

    @NonNull private String direction;
    @NonNull private WindDegree degree;
    @Nullable private Float speed;
    @NonNull private String level;

    public Wind(@NonNull String direction,
                @NonNull WindDegree degree,
                @Nullable Float speed,
                @NonNull String level) {
        this.direction = direction;
        this.degree = degree;
        this.speed = speed;
        this.level = level;
    }

    @NonNull
    public String getDirection() {
        return direction;
    }

    @NonNull
    public WindDegree getDegree() {
        return degree;
    }

    @Nullable
    public Float getSpeed() {
        return speed;
    }

    @NonNull
    public String getLevel() {
        return level;
    }

    @ColorRes
    public int getWindColorResId() {
        if (speed == null) {
            return 0;
        } else if (speed <= 30) {
            return 0;
        } else if (speed <= 51) {
            return 0;
        } else if (speed <= 75) {
            return 0;
        } else if (speed <= 103) {
            return R.color.colorLevel_4;
        } else if (speed <= 117) {
            return R.color.colorLevel_5;
        } else {
            return R.color.colorLevel_6;
        }
    }

    public String getWindDescription(SpeedUnit unit) {
        StringBuilder builder = new StringBuilder();
        builder.append(direction);
        if (speed != null) {
            builder.append(" ").append(unit.getSpeedText(speed));
        }
        builder.append(" ").append("(").append(level).append(")");
        if (!degree.isNoDirection()) {
            builder.append(" ").append(degree.getWindArrow());
        }
        return builder.toString();
    }
}

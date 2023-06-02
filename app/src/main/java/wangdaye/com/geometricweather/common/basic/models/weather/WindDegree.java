package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * WindDegree.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 * */
public class WindDegree implements Serializable {

    private final Float degree;
    private final Boolean noDirection;

    public WindDegree(Float degree, Boolean noDirection) {
        this.degree = degree;
        this.noDirection = noDirection;
    }

    public Float getDegree() {
        return degree;
    }

    public boolean isNoDirection() {
        return noDirection;
    }

    @Nullable
    public String getWindArrow() {
        if (noDirection) {
            return null;
        } else if (22.5 < degree && degree <= 67.5) {
            return "↙";
        } else if (67.5 < degree && degree <= 112.5) {
            return "←";
        } else if (112.5 < degree && degree <= 157.5) {
            return "↖";
        } else if (157.5 < degree && degree <= 202.5) {
            return "↑";
        } else if (202.5 < degree && degree <= 247.5) {
            return "↗";
        } else if (247.5 < degree && degree <= 292.5) {
            return "→";
        } else if (292. < degree && degree <= 337.5) {
            return "↘";
        } else {
            return "↓";
        }
    }
}

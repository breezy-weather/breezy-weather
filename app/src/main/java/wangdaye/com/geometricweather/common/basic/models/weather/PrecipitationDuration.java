package wangdaye.com.geometricweather.common.basic.models.weather;

import androidx.annotation.Nullable;

import java.io.Serializable;

import wangdaye.com.geometricweather.common.basic.models.options.unit.DurationUnit;

/**
 * Precipitation duration.
 *
 * default unit : {@link DurationUnit#H}
 * */
public class PrecipitationDuration implements Serializable {

    @Nullable private final Float total;
    @Nullable private final Float thunderstorm;
    @Nullable private final Float rain;
    @Nullable private final Float snow;
    @Nullable private final Float ice;

    public PrecipitationDuration(@Nullable Float total,
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
}

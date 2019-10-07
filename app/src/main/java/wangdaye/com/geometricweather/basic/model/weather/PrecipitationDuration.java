package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.Nullable;

/**
 * Precipitation duration.
 *
 * default unit : {@link wangdaye.com.geometricweather.basic.model.option.unit.DurationUnit#H}
 * */
public class PrecipitationDuration {

    @Nullable private Float total;
    @Nullable private Float thunderstorm;
    @Nullable private Float rain;
    @Nullable private Float snow;
    @Nullable private Float ice;

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

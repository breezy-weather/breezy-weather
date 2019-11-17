package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.Nullable;

/**
 * Precipitation duration.
 *
 * default unit : {@link wangdaye.com.geometricweather.basic.model.option.unit.ProbabilityUnit#PERCENT}
 * */
public class PrecipitationProbability {

    @Nullable private Float total;
    @Nullable private Float thunderstorm;
    @Nullable private Float rain;
    @Nullable private Float snow;
    @Nullable private Float ice;

    public PrecipitationProbability(@Nullable Float total,
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
}

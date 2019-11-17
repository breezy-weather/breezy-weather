package wangdaye.com.geometricweather.basic.model.weather;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;

/**
 * Temperature.
 * default unit : {@link wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit#C}
 * */
public class Temperature {

    private int temperature;
    @Nullable private Integer realFeelTemperature;
    @Nullable private Integer realFeelShaderTemperature;
    @Nullable private Integer apparentTemperature;
    @Nullable private Integer windChillTemperature;
    @Nullable private Integer wetBulbTemperature;
    @Nullable private Integer degreeDayTemperature;

    public Temperature(int temperature,
                       @Nullable Integer realFeelTemperature,
                       @Nullable Integer realFeelShaderTemperature,
                       @Nullable Integer apparentTemperature,
                       @Nullable Integer windChillTemperature,
                       @Nullable Integer wetBulbTemperature,
                       @Nullable Integer degreeDayTemperature) {
        this.temperature = temperature;
        this.realFeelTemperature = realFeelTemperature;
        this.realFeelShaderTemperature = realFeelShaderTemperature;
        this.apparentTemperature = apparentTemperature;
        this.windChillTemperature = windChillTemperature;
        this.wetBulbTemperature = wetBulbTemperature;
        this.degreeDayTemperature = degreeDayTemperature;
    }

    public int getTemperature() {
        return temperature;
    }

    @Nullable
    public Integer getRealFeelTemperature() {
        return realFeelTemperature;
    }

    @Nullable
    public Integer getRealFeelShaderTemperature() {
        return realFeelShaderTemperature;
    }

    @Nullable
    public Integer getApparentTemperature() {
        return apparentTemperature;
    }

    @Nullable
    public Integer getWindChillTemperature() {
        return windChillTemperature;
    }

    @Nullable
    public Integer getWetBulbTemperature() {
        return wetBulbTemperature;
    }

    @Nullable
    public Integer getDegreeDayTemperature() {
        return degreeDayTemperature;
    }

    @Nullable
    public String getTemperature(TemperatureUnit unit) {
        return getTemperature(temperature, unit);
    }

    @Nullable
    public String getShortTemperature(TemperatureUnit unit) {
        return getShortTemperature(temperature, unit);
    }

    @Nullable
    public String getRealFeelTemperature(TemperatureUnit unit) {
        return getTemperature(realFeelTemperature, unit);
    }

    @Nullable
    public String getShortRealFeeTemperature(TemperatureUnit unit) {
        return getShortTemperature(realFeelTemperature, unit);
    }

    @Nullable
    public static String getTemperature(@Nullable Integer temperature, TemperatureUnit unit) {
        if (temperature == null) {
            return null;
        }
        return unit.getTemperatureText(temperature);
    }

    @Nullable
    public static String getShortTemperature(@Nullable Integer temperature, TemperatureUnit unit) {
        if (temperature == null) {
            return null;
        }
        return unit.getTemperature(temperature) + "°";
    }

    @Nullable
    public static String getTrendTemperature(@Nullable Integer from, @Nullable Integer to,
                                             TemperatureUnit unit) {
        if (from == null || to == null) {
            return null;
        }
        return getShortTemperature(from, unit) + "/" + getShortTemperature(to, unit);
    }

    public boolean isValid() {
        return realFeelTemperature != null
                || realFeelShaderTemperature != null
                || apparentTemperature != null
                || windChillTemperature != null
                || wetBulbTemperature != null
                || degreeDayTemperature != null;
    }
}

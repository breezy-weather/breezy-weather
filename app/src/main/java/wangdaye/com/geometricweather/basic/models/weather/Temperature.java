package wangdaye.com.geometricweather.basic.models.weather;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.Serializable;

import wangdaye.com.geometricweather.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Temperature.
 * default unit : {@link wangdaye.com.geometricweather.basic.models.options.unit.TemperatureUnit#C}
 * */
public class Temperature implements Serializable {

    private final int temperature;
    @Nullable private final Integer realFeelTemperature;
    @Nullable private final Integer realFeelShaderTemperature;
    @Nullable private final Integer apparentTemperature;
    @Nullable private final Integer windChillTemperature;
    @Nullable private final Integer wetBulbTemperature;
    @Nullable private final Integer degreeDayTemperature;

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
    public String getTemperature(Context context, TemperatureUnit unit) {
        return getTemperature(context, temperature, unit);
    }

    @Nullable
    public String getShortTemperature(Context context, TemperatureUnit unit) {
        return getShortTemperature(context, temperature, unit);
    }

    @Nullable
    public String getRealFeelTemperature(Context context, TemperatureUnit unit) {
        return getTemperature(context, realFeelTemperature, unit);
    }

    @Nullable
    public String getShortRealFeeTemperature(Context context, TemperatureUnit unit) {
        return getShortTemperature(context, realFeelTemperature, unit);
    }

    @Nullable
    public static String getTemperature(Context context, @Nullable Integer temperature, TemperatureUnit unit) {
        if (temperature == null) {
            return null;
        }
        return unit.getTemperatureText(context, temperature);
    }

    @Nullable
    public static String getShortTemperature(Context context,
                                             @Nullable Integer temperature, TemperatureUnit unit) {
        if (temperature == null) {
            return null;
        }
        return unit.getShortTemperatureText(context, temperature);
    }

    @Nullable
    public static String getTrendTemperature(Context context,
                                             @Nullable Integer night, @Nullable Integer day,
                                             TemperatureUnit unit) {
        if (night == null || day == null) {
            return null;
        }
        if (SettingsOptionManager.getInstance(context).isExchangeDayNightTempEnabled()) {
            return getShortTemperature(context, day, unit) + "/" + getShortTemperature(context, night, unit);
        } else {
            return getShortTemperature(context, night, unit) + "/" + getShortTemperature(context, day, unit);
        }
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

package wangdaye.com.geometricweather.common.basic.models.options.provider;

import android.content.Context;

import androidx.annotation.ColorInt;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options._utils.Utils;

public enum WeatherSource {

    ACCU("accu", 0xffef5823, "accuweather.com"),
    OWM("owm", 0xffeb6e4b, "openweathermap.org"),
    MF("mf", 0xff005892, "meteofrance.com"),
    CN("cn", 0xff033566, "weather.com.cn"),
    CAIYUN("caiyun", 0xff5ebb8e, " caiyunapp.com");

    private final String sourceId;
    @ColorInt private final int sourceColor;
    private final String sourceUrl;

    WeatherSource(String id, @ColorInt int color, String url) {
        sourceId = id;
        sourceColor = color;
        sourceUrl = url;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                sourceId,
                R.array.weather_sources,
                R.array.weather_source_values
        );
    }

    public String getSourceVoice(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                sourceId,
                R.array.weather_source_voices,
                R.array.weather_source_values
        );
    }

    public int getSourceColor() {
        return sourceColor;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public static WeatherSource getInstance(String value) {
        switch (value) {
            case "owm":
                return OWM;

            case "mf":
                return MF;

            case "cn":
                return CN;

            case "caiyun":
                return CAIYUN;

            default:
                return ACCU;
        }
    }
}

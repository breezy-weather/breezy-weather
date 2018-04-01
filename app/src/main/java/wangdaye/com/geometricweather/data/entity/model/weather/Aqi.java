package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.data.entity.result.accu.AccuAqiResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Aqi.
 * */

public class Aqi {

    public String aqi;
    public String pm25;
    public String pm10;
    public String quality;

    Aqi() {}

/*
    void buildAqi(FWResult result) {
        aqi = result.pm25.aqi;
        pm25 = result.pm25.pm25;
        pm10 = result.pm25.pm10;
        quality = result.pm25.quality;
    }
*/

    public void buildAqi(Context c, AccuAqiResult result) {
        if (result == null) {
            aqi = "";
            pm25 = "";
            pm10 = "";
            quality = "";
        } else {
            aqi = String.valueOf(result.Index);
            pm25 = String.valueOf(result.ParticulateMatter2_5);
            pm10 = String.valueOf(result.ParticulateMatter10);
            quality = WeatherHelper.getAqiQuality(c, result.Index);
        }
    }

    public void buildAqi(Context c, CNWeatherResult result) {
        aqi = String.valueOf(result.pm25.aqi);
        pm25 = String.valueOf(result.pm25.pm25);
        pm10 = String.valueOf(result.pm25.pm10);
        quality = WeatherHelper.getAqiQuality(c, result.pm25.aqi);
    }

    void buildAqi(WeatherEntity entity) {
        aqi = entity.aqiAqi;
        pm25 = entity.aqiPm25;
        pm10 = entity.aqiPm10;
        quality = entity.aqiQuality;
    }
}

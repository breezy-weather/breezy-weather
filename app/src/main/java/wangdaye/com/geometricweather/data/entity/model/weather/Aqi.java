package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.data.entity.result.accu.AccuAqiResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Aqi.
 * */

public class Aqi {

    public String quality;
    public int aqi;
    public int pm25;
    public int pm10;
    public int so2;
    public int no2;
    public int o3;
    public float co;

    Aqi() {
        quality = "";
        aqi = -1;
        pm25 = -1;
        pm10 = -1;
        so2 = -1;
        no2 = -1;
        o3 = -1;
        co = -1;
    }

    public void buildAqi(Context c, @Nullable AccuAqiResult result) {
        if (result == null) {
            quality = "";
            aqi = -1;
            pm25 = -1;
            pm10 = -1;
            so2 = -1;
            no2 = -1;
            o3 = -1;
            co = -1;
        } else {
            quality = WeatherHelper.getAqiQuality(c, result.Index);
            aqi = result.Index;
            pm25 = (int) result.ParticulateMatter2_5;
            pm10 = (int) result.ParticulateMatter10;
            so2 = (int) result.SulfurDioxide;
            no2 = (int) result.NitrogenDioxide;
            o3 = (int) result.Ozone;
            co = (int) result.CarbonMonoxide;
        }
    }

    public void buildAqi(Context c, CNWeatherResult result) {
        quality = WeatherHelper.getAqiQuality(c, result.pm25.aqi);
        aqi = result.pm25.aqi;
        pm25 = result.pm25.pm25;
        pm10 = result.pm25.pm10;
        so2 = result.pm25.so2;
        no2 = result.pm25.no2;
        o3 = result.pm25.o3;
        co = Float.parseFloat(result.pm25.co);
    }

    public void buildAqi(Context c, CaiYunMainlyResult result) {
        quality = WeatherHelper.getAqiQuality(c, Integer.parseInt(result.aqi.aqi));

        try {
            aqi = (int) Double.parseDouble(result.aqi.aqi);
        } catch (Exception e) {
            aqi = -1;
        }

        try {
            pm25 = (int) Double.parseDouble(result.aqi.pm25);
        } catch (Exception e) {
            pm25 = -1;
        }

        try {
            pm10 = (int) Double.parseDouble(result.aqi.pm10);
        } catch (Exception e) {
            pm10 = -1;
        }

        try {
            so2 = (int) Double.parseDouble(result.aqi.so2);
        } catch (Exception e) {
            so2 = -1;
        }

        try {
            no2 = (int) Double.parseDouble(result.aqi.no2);
        } catch (Exception e) {
            no2 = -1;
        }

        try {
            o3 = (int) Double.parseDouble(result.aqi.o3);
        } catch (Exception e) {
            o3 = -1;
        }

        try {
            co = Float.parseFloat(result.aqi.co);
        } catch (Exception e) {
            co = -1;
        }
    }

    void buildAqi(WeatherEntity entity) {
        quality = entity.aqiQuality;
        aqi = entity.aqiAqi;
        pm25 = entity.aqiPm25;
        pm10 = entity.aqiPm10;
        so2 = entity.aqiSo2;
        no2 = entity.aqiNo2;
        o3 = entity.aqiO3;
        co = entity.aqiCo;
    }
}
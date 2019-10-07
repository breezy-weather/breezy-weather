package wangdaye.com.geometricweather.weather.service;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.List;

import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.basic.model.location.Location;

/**
 * Weather service.
 * */

public abstract class WeatherService {

    public abstract void requestWeather(Context context, Location location,
                                        @NonNull RequestWeatherCallback callback);

    @WorkerThread
    @NonNull
    public abstract List<Location> requestLocation(Context context, String query);

    public abstract void requestLocation(Context context, Location location,
                                         @NonNull RequestLocationCallback callback);

    public abstract void cancel();

    protected static String formatLocationString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        if (str.endsWith("地区")) {
            return str.substring(0, str.length() - 2);
        }
        if (str.endsWith("区")
                && !str.endsWith("新区")
                && !str.endsWith("矿区")
                && !str.endsWith("郊区")
                && !str.endsWith("风景区")
                && !str.endsWith("东区")
                && !str.endsWith("西区")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("县")
                && str.length() != 2
                && !str.endsWith("通化县")
                && !str.endsWith("本溪县")
                && !str.endsWith("辽阳县")
                && !str.endsWith("建平县")
                && !str.endsWith("承德县")
                && !str.endsWith("大同县")
                && !str.endsWith("五台县")
                && !str.endsWith("乌鲁木齐县")
                && !str.endsWith("伊宁县")
                && !str.endsWith("南昌县")
                && !str.endsWith("上饶县")
                && !str.endsWith("吉安县")
                && !str.endsWith("长沙县")
                && !str.endsWith("衡阳县")
                && !str.endsWith("邵阳县")
                && !str.endsWith("宜宾县")) {
            return str.substring(0, str.length() - 1);
        }

        if (str.endsWith("市")
                && !str.endsWith("新市")
                && !str.endsWith("沙市")
                && !str.endsWith("津市")
                && !str.endsWith("芒市")
                && !str.endsWith("西市")
                && !str.endsWith("峨眉山市")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("回族自治州")
                || str.endsWith("藏族自治州")
                || str.endsWith("彝族自治州")
                || str.endsWith("白族自治州")
                || str.endsWith("傣族自治州")
                || str.endsWith("蒙古自治州")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("朝鲜族自治州")
                || str.endsWith("哈萨克自治州")
                || str.endsWith("傈僳族自治州")
                || str.endsWith("蒙古族自治州")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("哈萨克族自治州")
                || str.endsWith("苗族侗族自治州")
                || str.endsWith("藏族羌族自治州")
                || str.endsWith("壮族苗族自治州")
                || str.endsWith("柯尔克孜自治州")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("布依族苗族自治州")
                || str.endsWith("土家族苗族自治州")
                || str.endsWith("蒙古族藏族自治州")
                || str.endsWith("柯尔克孜族自治州")
                || str.endsWith("傣族景颇族自治州")
                || str.endsWith("哈尼族彝族自治州")) {
            return str.substring(0, str.length() - 8);
        }
        if (str.endsWith("自治州")) {
            return str.substring(0, str.length() - 3);
        }

        if (str.endsWith("省")) {
            return str.substring(0, str.length() - 1);
        }
        if (str.endsWith("壮族自治区") || str.endsWith("回族自治区")) {
            return str.substring(0, str.length() - 5);
        }
        if (str.endsWith("维吾尔自治区")) {
            return str.substring(0, str.length() - 6);
        }
        if (str.endsWith("维吾尔族自治区")) {
            return str.substring(0, str.length() - 7);
        }
        if (str.endsWith("自治区")) {
            return str.substring(0, str.length() - 3);
        }
        return str;
    }

    protected static String convertChinese(String text) {
        try {
            return LanguageUtils.traditionalToSimplified(text);
        } catch (Exception e) {
            return text;
        }
    }

    public interface RequestWeatherCallback {
        void requestWeatherSuccess(@NonNull Location requestLocation);
        void requestWeatherFailed(@NonNull Location requestLocation);
    }

    public interface RequestLocationCallback {
        void requestLocationSuccess(String query, List<Location> locationList);
        void requestLocationFailed(String query);
    }
}

package wangdaye.com.geometricweather.data.entity.model.weather;

import android.content.Context;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuRealtimeResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Base.
 * */

public class Base {

    public String cityId;
    public String city;
    public String date;
    public String time;
    public long timeStamp;

    Base() {}

/*
    void buildBase(FWResult result) {
        cityId = "CN" + result.cityid;
        city = result.city;
        date = result.realtime.time.split(" ")[0];
        time = result.realtime.time.split(" ")[1].split(":")[0]
                + ":" + result.realtime.time.split(" ")[1].split(":")[1];
    }

    void buildBase(HefengResult result, int p) {
        cityId = result.heWeather.get(p).basic.id;
        city = result.heWeather.get(p).basic.city;
        date = result.heWeather.get(p).basic.updateRotation.loc.split(" ")[0];
        time = result.heWeather.get(p).basic.updateRotation.loc.split(" ")[1];
    }
*/

    public void buildBase(Context c, Location location, AccuRealtimeResult result) {
        cityId = location.cityId;
        city = location.city;
        date = result.LocalObservationDateTime.split("T")[0];
        time = buildTime(
                c,
                result.LocalObservationDateTime.split("T")[1].split(":")[0],
                result.LocalObservationDateTime.split("T")[1].split(":")[1]);
        timeStamp = System.currentTimeMillis();
    }

    public void buildBase(Context c, Location location, CNWeatherResult result) {
        cityId = location.cityId;
        city = location.city;
        date = result.realtime.date;
        time = buildTime(
                c,
                result.realtime.time.split(":")[0],
                result.realtime.time.split(":")[1]);
        timeStamp = System.currentTimeMillis();
    }

    void buildBase(WeatherEntity entity) {
        cityId = entity.cityId;
        city = entity.city;
        date = entity.date;
        time = entity.time;
        timeStamp = entity.timeStamp;
    }

    private String buildTime(Context c, String hourString, String minuteString) {
        if (TimeManager.is12Hour(c)) {
            try {
                int hour = Integer.parseInt(hourString);
                if (hour == 0) {
                    hour = 24;
                }
                String suffix = hour < 13 ? "AM" : "PM";
                if (hour > 12) {
                    hour -= 12;
                }
                return hour + ":" + minuteString + " " + suffix;
            } catch (Exception ignored) {
                // do nothing.
            }
        }
        return hourString + ":" + minuteString;
    }
}

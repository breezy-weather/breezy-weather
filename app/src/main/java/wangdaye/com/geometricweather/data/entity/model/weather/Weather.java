package wangdaye.com.geometricweather.data.entity.model.weather;


import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;

/**
 * Weather
 * */

public class Weather {

    public Base base;
    public RealTime realTime;
    public List<Daily> dailyList;
    public List<Hourly> hourlyList;
    public Aqi aqi;
    public Index index;
    public List<Alert> alertList;

    public Weather() {
        this.base = new Base();
        this.realTime = new RealTime();
        this.dailyList = new ArrayList<>();
        this.hourlyList = new ArrayList<>();
        this.aqi = new Aqi();
        this.index = new Index();
        this.alertList = new ArrayList<>();
    }

    public boolean isValid(float hours) {
        return System.currentTimeMillis() - base.timeStamp < hours * 60 * 60 * 1000;
    }

/*
    public static Weather buildWeather(FWResult result) {
        if (result == null) {
            return null;
        }

        Weather weather = new Weather();
        weather.base.buildBase(result);
        weather.realTime.buildRealTime(result);
        for (int i = 0; i < result.weathers.size(); i ++) {
            weather.dailyList.add(new Daily().buildDaily(result.weathers.get(i)));
        }
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < result.weatherDetailsInfo.weather24HoursDetailsInfos.size(); i ++) {
            weather.hourlyList.add(new Hourly().buildHourly(
                    c, weather.dailyList.get(0), result.weatherDetailsInfo.weather24HoursDetailsInfos.get(i)));
        }
        weather.aqi.buildAqi(result);
        for (int i = 0; i < result.alarms.size(); i ++) {
            weather.alertList.add(new Alert().buildAlert(result.alarms.get(i)));
        }
        weather.index.buildIndex(weather.realTime, weather.dailyList.get(0), weather.hourlyList.get(0), weather.aqi, result);

        return weather;
    }

    public static Weather buildWeather(Weather oldResult, HefengResult result) {
        int p = HefengWeather.getLatestDataPosition(result);
        if (result == null
                || result.heWeather == null || result.heWeather.size() == 0
                || !result.heWeather.get(p).status.equals("ok")) {
            return null;
        }

        try {
            Weather weather = new Weather();

            weather.base.buildBase(result, p);
            weather.realTime.buildRealTime(result, p);
            for (int i = 0; i < result.heWeather.get(p).daily_forecast.size(); i ++) {
                weather.dailyList.add(new Daily().buildDaily(result.heWeather.get(p).daily_forecast.get(i)));
            }
            Calendar c = Calendar.getInstance();
            if (oldResult != null) {
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                String[] weatherDates = oldResult.base.date.split("-");
                if (weatherDates[0].equals(String.valueOf(year))
                        && weatherDates[1].equals(String.valueOf(month))
                        && weatherDates[2].equals(String.valueOf(day))) {
                    weather.hourlyList = oldResult.hourlyList;
                }
            }
            if (weather.hourlyList.size() == 0) {
                for (int i = 0; i < result.heWeather.get(p).hourly_forecast.size(); i ++) {
                    weather.hourlyList.add(new Hourly().buildHourly(
                            c, weather.dailyList.get(0), result.heWeather.get(p).hourly_forecast.get(i)));
                }
            }
            weather.index.buildIndex(weather.realTime, weather.dailyList.get(0), weather.hourlyList.get(0));

            return weather;
        } catch (NullPointerException e) {
            return null;
        }
    }
*/

    public static Weather buildWeatherPrimaryData(WeatherEntity entity) {
        Weather weather = new Weather();
        weather.base.buildBase(entity);
        weather.realTime.buildRealTime(entity);
        weather.aqi.buildAqi(entity);
        weather.index.buildIndex(entity);
        return weather;
    }

    public Weather buildWeatherDailyList(List<DailyEntity> list) {
        for (int i = 0; i < list.size(); i ++) {
            dailyList.add(new Daily().buildDaily(list.get(i)));
        }
        return this;
    }

    public Weather buildWeatherHourlyList(List<HourlyEntity> list) {
        for (int i = 0; i < list.size(); i ++) {
            hourlyList.add(new Hourly().buildHourly(list.get(i)));
        }
        return this;
    }

    public Weather buildWeatherAlarmList(List<AlarmEntity> list) {
        for (int i = 0; i < list.size(); i ++) {
            alertList.add(new Alert().buildAlert(list.get(i)));
        }
        return this;
    }
}

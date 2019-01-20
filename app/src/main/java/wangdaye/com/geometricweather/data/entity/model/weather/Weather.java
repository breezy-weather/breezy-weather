package wangdaye.com.geometricweather.data.entity.model.weather;

import java.util.ArrayList;
import java.util.List;

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
        return Math.abs(System.currentTimeMillis() - base.timeStamp) < hours * 60 * 60 * 1000;
    }

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

package wangdaye.com.geometricweather.basic.model.weather;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.db.entity.AlarmEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.entity.HourlyEntity;

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

    public Weather(Base base, RealTime realTime,
                   List<Daily> dailyList, List<Hourly> hourlyList,
                   Aqi aqi, Index index,
                   List<Alert> alertList) {
        this.base = base;
        this.realTime = realTime;
        this.dailyList = dailyList;
        this.hourlyList = hourlyList;
        this.aqi = aqi;
        this.index = index;
        this.alertList = alertList;
    }

    public boolean isValid(float hours) {
        return Math.abs(System.currentTimeMillis() - base.timeStamp) < hours * 60 * 60 * 1000;
    }

    public List<AlarmEntity> toAlarmEntityList() {
        List<AlarmEntity> entityList = new ArrayList<>(alertList.size());
        for (int i = 0; i < alertList.size(); i ++) {
            AlarmEntity entity = new AlarmEntity();
            entity.cityId = base.cityId;
            entity.city = base.city;
            entity.alertId = alertList.get(i).id;
            entity.content = alertList.get(i).content;
            entity.description = alertList.get(i).description;
            entity.publishTime = alertList.get(i).publishTime;
            entityList.add(entity);
        }
        return entityList;
    }

    public List<DailyEntity> toDailyEntityList() {
        List<DailyEntity> entityList = new ArrayList<>(dailyList.size());
        for (int i = 0; i < dailyList.size(); i ++) {
            DailyEntity entity = new DailyEntity();
            entity.cityId = base.cityId;
            entity.city = base.city;
            entity.date = dailyList.get(i).date;
            entity.week = dailyList.get(i).week;
            entity.daytimeWeather = dailyList.get(i).weathers[0];
            entity.nighttimeWeather = dailyList.get(i).weathers[1];
            entity.daytimeWeatherKind = dailyList.get(i).weatherKinds[0];
            entity.nighttimeWeatherKind = dailyList.get(i).weatherKinds[1];
            entity.maxiTemp = dailyList.get(i).temps[0];
            entity.miniTemp = dailyList.get(i).temps[1];
            entity.daytimeWindDir = dailyList.get(i).windDirs[0];
            entity.nighttimeWindDir = dailyList.get(i).windDirs[1];
            entity.daytimeWindSpeed = dailyList.get(i).windSpeeds[0];
            entity.nighttimeWindSpeed = dailyList.get(i).windSpeeds[1];
            entity.daytimeWindLevel = dailyList.get(i).windLevels[0];
            entity.nighttimeWindLevel = dailyList.get(i).windLevels[1];
            entity.daytimeWindDegree = dailyList.get(i).windDegrees[0];
            entity.nighttimeWindDegree = dailyList.get(i).windDegrees[1];
            entity.sunrise = dailyList.get(i).astros[0];
            entity.sunset = dailyList.get(i).astros[1];
            entity.moonrise = dailyList.get(i).astros[2];
            entity.moonset = dailyList.get(i).astros[3];
            entity.moonPhase = dailyList.get(i).moonPhase;
            entity.sunset = dailyList.get(i).astros[1];
            entity.daytimePrecipitations = dailyList.get(i).precipitations[0];
            entity.nighttimePrecipitations = dailyList.get(i).precipitations[1];
            entityList.add(entity);
        }
        return entityList;
    }

    public List<HourlyEntity> toHourlyEntityList() {
        List<HourlyEntity> entityList = new ArrayList<>(hourlyList.size());
        for (int i = 0; i < hourlyList.size(); i ++) {
            HourlyEntity entity = new HourlyEntity();
            entity.cityId = base.cityId;
            entity.city = base.city;
            entity.time = hourlyList.get(i).time;
            entity.dayTime = hourlyList.get(i).dayTime;
            entity.weather = hourlyList.get(i).weather;
            entity.weatherKind = hourlyList.get(i).weatherKind;
            entity.temp = hourlyList.get(i).temp;
            entity.precipitation = hourlyList.get(i).precipitation;
            entityList.add(entity);
        }
        return entityList;
    }
}

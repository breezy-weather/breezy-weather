package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Daily entity.
 * */

@Entity
public class DailyEntity {
    // data
    @Id
    public Long id;
    public String cityId;
    public String city;

    public String date;
    public String week;
    public String daytimeWeather;
    public String nighttimeWeather;
    public String daytimeWeatherKind;
    public String nighttimeWeatherKind;
    public int maxiTemp;
    public int miniTemp;
    public String daytimeWindDir;
    public String nighttimeWindDir;
    public String daytimeWindSpeed;
    public String nighttimeWindSpeed;
    public String daytimeWindLevel;
    public String nighttimeWindLevel;
    public int daytimeWindDegree;
    public int nighttimeWindDegree;
    public String sunrise;
    public String sunset;
    public int daytimePrecipitations;
    public int nighttimePrecipitations;

    @Generated(hash = 1398898456)
    public DailyEntity(Long id, String cityId, String city, String date, String week, String daytimeWeather,
            String nighttimeWeather, String daytimeWeatherKind, String nighttimeWeatherKind, int maxiTemp,
            int miniTemp, String daytimeWindDir, String nighttimeWindDir, String daytimeWindSpeed,
            String nighttimeWindSpeed, String daytimeWindLevel, String nighttimeWindLevel, int daytimeWindDegree,
            int nighttimeWindDegree, String sunrise, String sunset, int daytimePrecipitations,
            int nighttimePrecipitations) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.date = date;
        this.week = week;
        this.daytimeWeather = daytimeWeather;
        this.nighttimeWeather = nighttimeWeather;
        this.daytimeWeatherKind = daytimeWeatherKind;
        this.nighttimeWeatherKind = nighttimeWeatherKind;
        this.maxiTemp = maxiTemp;
        this.miniTemp = miniTemp;
        this.daytimeWindDir = daytimeWindDir;
        this.nighttimeWindDir = nighttimeWindDir;
        this.daytimeWindSpeed = daytimeWindSpeed;
        this.nighttimeWindSpeed = nighttimeWindSpeed;
        this.daytimeWindLevel = daytimeWindLevel;
        this.nighttimeWindLevel = nighttimeWindLevel;
        this.daytimeWindDegree = daytimeWindDegree;
        this.nighttimeWindDegree = nighttimeWindDegree;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.daytimePrecipitations = daytimePrecipitations;
        this.nighttimePrecipitations = nighttimePrecipitations;
    }

    @Generated(hash = 1809948821)
    public DailyEntity() {
    }

    /** <br> life cycle. */

    private static List<DailyEntity> buildDailyEntityList(Weather weather) {
        List<DailyEntity> entityList = new ArrayList<>(weather.dailyList.size());
        for (int i = 0; i < weather.dailyList.size(); i ++) {
            DailyEntity entity = new DailyEntity();
            entity.cityId = weather.base.cityId;
            entity.city = weather.base.city;
            entity.date = weather.dailyList.get(i).date;
            entity.week = weather.dailyList.get(i).week;
            entity.daytimeWeather = weather.dailyList.get(i).weathers[0];
            entity.nighttimeWeather = weather.dailyList.get(i).weathers[1];
            entity.daytimeWeatherKind = weather.dailyList.get(i).weatherKinds[0];
            entity.nighttimeWeatherKind = weather.dailyList.get(i).weatherKinds[1];
            entity.maxiTemp = weather.dailyList.get(i).temps[0];
            entity.miniTemp = weather.dailyList.get(i).temps[1];
            entity.daytimeWindDir = weather.dailyList.get(i).windDirs[0];
            entity.nighttimeWindDir = weather.dailyList.get(i).windDirs[1];
            entity.daytimeWindSpeed = weather.dailyList.get(i).windSpeeds[0];
            entity.nighttimeWindSpeed = weather.dailyList.get(i).windSpeeds[1];
            entity.daytimeWindLevel = weather.dailyList.get(i).windLevels[0];
            entity.nighttimeWindLevel = weather.dailyList.get(i).windLevels[1];
            entity.daytimeWindDegree = weather.dailyList.get(i).windDegrees[0];
            entity.nighttimeWindDegree = weather.dailyList.get(i).windDegrees[1];
            entity.sunrise = weather.dailyList.get(i).astros[0];
            entity.sunset = weather.dailyList.get(i).astros[1];
            entity.daytimePrecipitations = weather.dailyList.get(i).precipitations[0];
            entity.nighttimePrecipitations = weather.dailyList.get(i).precipitations[1];
            entityList.add(entity);
        }
        return entityList;
    }

    /** <br> database. */

    // insert.

    public static void insertDailyList(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        deleteDailyEntityList(
                database, 
                searchLocationDailyEntity(database, location));   
        
        List<DailyEntity> entityList = buildDailyEntityList(weather);
        new DaoMaster(database)
                .newSession()
                .getDailyEntityDao()
                .insertInTx(entityList);
    }

    // delete.

    private static void deleteDailyEntityList(SQLiteDatabase database, List<DailyEntity> list) {
        new DaoMaster(database)
                .newSession()
                .getDailyEntityDao()
                .deleteInTx(list);
    }

    // search.
    
    public static List<DailyEntity> searchLocationDailyEntity(SQLiteDatabase database, Location location) {
        return new DaoMaster(database)
                .newSession()
                .getDailyEntityDao()
                .queryBuilder()
                .where(DailyEntityDao.Properties.CityId.eq(location.cityId))
                .list();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityId() {
        return this.cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeek() {
        return this.week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDaytimeWeather() {
        return this.daytimeWeather;
    }

    public void setDaytimeWeather(String daytimeWeather) {
        this.daytimeWeather = daytimeWeather;
    }

    public String getNighttimeWeather() {
        return this.nighttimeWeather;
    }

    public void setNighttimeWeather(String nighttimeWeather) {
        this.nighttimeWeather = nighttimeWeather;
    }

    public String getDaytimeWeatherKind() {
        return this.daytimeWeatherKind;
    }

    public void setDaytimeWeatherKind(String daytimeWeatherKind) {
        this.daytimeWeatherKind = daytimeWeatherKind;
    }

    public String getNighttimeWeatherKind() {
        return this.nighttimeWeatherKind;
    }

    public void setNighttimeWeatherKind(String nighttimeWeatherKind) {
        this.nighttimeWeatherKind = nighttimeWeatherKind;
    }

    public int getMaxiTemp() {
        return this.maxiTemp;
    }

    public void setMaxiTemp(int maxiTemp) {
        this.maxiTemp = maxiTemp;
    }

    public int getMiniTemp() {
        return this.miniTemp;
    }

    public void setMiniTemp(int miniTemp) {
        this.miniTemp = miniTemp;
    }

    public String getDaytimeWindDir() {
        return this.daytimeWindDir;
    }

    public void setDaytimeWindDir(String daytimeWindDir) {
        this.daytimeWindDir = daytimeWindDir;
    }

    public String getNighttimeWindDir() {
        return this.nighttimeWindDir;
    }

    public void setNighttimeWindDir(String nighttimeWindDir) {
        this.nighttimeWindDir = nighttimeWindDir;
    }

    public String getDaytimeWindSpeed() {
        return this.daytimeWindSpeed;
    }

    public void setDaytimeWindSpeed(String daytimeWindSpeed) {
        this.daytimeWindSpeed = daytimeWindSpeed;
    }

    public String getNighttimeWindSpeed() {
        return this.nighttimeWindSpeed;
    }

    public void setNighttimeWindSpeed(String nighttimeWindSpeed) {
        this.nighttimeWindSpeed = nighttimeWindSpeed;
    }

    public String getDaytimeWindLevel() {
        return this.daytimeWindLevel;
    }

    public void setDaytimeWindLevel(String daytimeWindLevel) {
        this.daytimeWindLevel = daytimeWindLevel;
    }

    public String getNighttimeWindLevel() {
        return this.nighttimeWindLevel;
    }

    public void setNighttimeWindLevel(String nighttimeWindLevel) {
        this.nighttimeWindLevel = nighttimeWindLevel;
    }

    public int getDaytimeWindDegree() {
        return this.daytimeWindDegree;
    }

    public void setDaytimeWindDegree(int daytimeWindDegree) {
        this.daytimeWindDegree = daytimeWindDegree;
    }

    public int getNighttimeWindDegree() {
        return this.nighttimeWindDegree;
    }

    public void setNighttimeWindDegree(int nighttimeWindDegree) {
        this.nighttimeWindDegree = nighttimeWindDegree;
    }

    public String getSunrise() {
        return this.sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return this.sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public int getDaytimePrecipitations() {
        return this.daytimePrecipitations;
    }

    public void setDaytimePrecipitations(int daytimePrecipitations) {
        this.daytimePrecipitations = daytimePrecipitations;
    }

    public int getNighttimePrecipitations() {
        return this.nighttimePrecipitations;
    }

    public void setNighttimePrecipitations(int nighttimePrecipitations) {
        this.nighttimePrecipitations = nighttimePrecipitations;
    }
}

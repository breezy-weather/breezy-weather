package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;
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
    public String windDir;
    public String windLevel;
    public String sunrise;
    public String sunset;

    @Generated(hash = 800568837)
    public DailyEntity(Long id, String cityId, String city, String date, String week, String daytimeWeather, String nighttimeWeather,
            String daytimeWeatherKind, String nighttimeWeatherKind, int maxiTemp, int miniTemp, String windDir, String windLevel,
            String sunrise, String sunset) {
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
        this.windDir = windDir;
        this.windLevel = windLevel;
        this.sunrise = sunrise;
        this.sunset = sunset;
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
            entity.windDir = weather.dailyList.get(i).windDir;
            entity.windLevel = weather.dailyList.get(i).windLevel;
            entity.sunrise = weather.dailyList.get(i).astros[0];
            entity.sunset = weather.dailyList.get(i).astros[1];
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
        DailyEntityDao dao = new DaoMaster(database)
                .newSession()
                .getDailyEntityDao();
        for (int i = 0; i < entityList.size(); i ++) {
            dao.insert(entityList.get(i));
        }
    }

    // delete.

    private static void deleteDailyEntityList(SQLiteDatabase database, List<DailyEntity> list) {
        for (int i = 0; i < list.size(); i ++) {
            new DaoMaster(database)
                    .newSession()
                    .getDailyEntityDao()
                    .delete(list.get(i));
        }
    }

    // search.
    
    public static List<DailyEntity> searchLocationDailyEntity(SQLiteDatabase database, Location location) {
        return new DaoMaster(database)
                .newSession()
                .getDailyEntityDao()
                .queryBuilder()
                .where(location.isEngLocation() ?
                        DailyEntityDao.Properties.City.eq(location.city) : DailyEntityDao.Properties.CityId.eq(location.cityId))
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

    public String getWindDir() {
        return this.windDir;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public String getWindLevel() {
        return this.windLevel;
    }

    public void setWindLevel(String windLevel) {
        this.windLevel = windLevel;
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
}

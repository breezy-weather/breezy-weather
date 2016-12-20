package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;

import org.greenrobot.greendao.annotation.Id;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Weather entity.
 * */

@Entity
public class WeatherEntity {
    // data
    @Id
    public Long id;

    // base.
    public String cityId;
    public String city;
    public String date;
    public String time;

    // realTime.
    public String realTimeWeather;
    public String realTimeWeatherKind;
    public int realTimeTemp;
    public int realTimeSendibleTemp;
    public String realTimeWindDir;
    public String realTimeWindLevel;

    // aqi.
    public String aqiAqi;
    public String aqiRank;
    public String aqiPm25;
    public String aqiPm10;
    public String aqiQuality;
    public String aqiDescription;

    //life.
    public String lifeWindTitle;
    public String lifeWindContent;
    public String lifeAqiTitle;
    public String lifeAqiContent;
    public String lifeHumidityTitle;
    public String lifeHumidityContent;
    public String lifeUvTitle;
    public String lifeUvContent;
    public String lifeDressesTitle;
    public String lifeDressesContent;
    public String lifeExerciseTitle;
    public String lifeExerciseContent;
    public String lifeWashCarTitle;
    public String lifeWashCarContent;
    public String lifeColdTitle;
    public String lifeColdContent;

    @Generated(hash = 652081930)
    public WeatherEntity(Long id, String cityId, String city, String date, String time, String realTimeWeather,
            String realTimeWeatherKind, int realTimeTemp, int realTimeSendibleTemp, String realTimeWindDir, String realTimeWindLevel,
            String aqiAqi, String aqiRank, String aqiPm25, String aqiPm10, String aqiQuality, String aqiDescription,
            String lifeWindTitle, String lifeWindContent, String lifeAqiTitle, String lifeAqiContent, String lifeHumidityTitle,
            String lifeHumidityContent, String lifeUvTitle, String lifeUvContent, String lifeDressesTitle, String lifeDressesContent,
            String lifeExerciseTitle, String lifeExerciseContent, String lifeWashCarTitle, String lifeWashCarContent,
            String lifeColdTitle, String lifeColdContent) {
        this.id = id;
        this.cityId = cityId;
        this.city = city;
        this.date = date;
        this.time = time;
        this.realTimeWeather = realTimeWeather;
        this.realTimeWeatherKind = realTimeWeatherKind;
        this.realTimeTemp = realTimeTemp;
        this.realTimeSendibleTemp = realTimeSendibleTemp;
        this.realTimeWindDir = realTimeWindDir;
        this.realTimeWindLevel = realTimeWindLevel;
        this.aqiAqi = aqiAqi;
        this.aqiRank = aqiRank;
        this.aqiPm25 = aqiPm25;
        this.aqiPm10 = aqiPm10;
        this.aqiQuality = aqiQuality;
        this.aqiDescription = aqiDescription;
        this.lifeWindTitle = lifeWindTitle;
        this.lifeWindContent = lifeWindContent;
        this.lifeAqiTitle = lifeAqiTitle;
        this.lifeAqiContent = lifeAqiContent;
        this.lifeHumidityTitle = lifeHumidityTitle;
        this.lifeHumidityContent = lifeHumidityContent;
        this.lifeUvTitle = lifeUvTitle;
        this.lifeUvContent = lifeUvContent;
        this.lifeDressesTitle = lifeDressesTitle;
        this.lifeDressesContent = lifeDressesContent;
        this.lifeExerciseTitle = lifeExerciseTitle;
        this.lifeExerciseContent = lifeExerciseContent;
        this.lifeWashCarTitle = lifeWashCarTitle;
        this.lifeWashCarContent = lifeWashCarContent;
        this.lifeColdTitle = lifeColdTitle;
        this.lifeColdContent = lifeColdContent;
    }

    @Generated(hash = 1598697471)
    public WeatherEntity() {
    }

    /** <br> life cycle. */

    private static WeatherEntity buildWeatherEntity(Weather weather) {
        WeatherEntity entity = new WeatherEntity();

        // base.
        entity.cityId = weather.base.cityId;
        entity.city = weather.base.city;
        entity.date = weather.base.date;
        entity.time = weather.base.time;

        // realTime.
        entity.realTimeWeather = weather.realTime.weather;
        entity.realTimeWeatherKind = weather.realTime.weatherKind;
        entity.realTimeTemp = weather.realTime.temp;
        entity.realTimeSendibleTemp = weather.realTime.sendibleTemp;
        entity.realTimeWindDir = weather.realTime.windDir;
        entity.realTimeWindLevel = weather.realTime.windLevel;

        // aqi.
        entity.aqiAqi = weather.aqi.aqi;
        entity.aqiRank = weather.aqi.rank;
        entity.aqiPm25 = weather.aqi.pm25;
        entity.aqiPm10 = weather.aqi.pm10;
        entity.aqiQuality = weather.aqi.quality;
        entity.aqiDescription = weather.aqi.description;

        //life.
        if (weather.life.winds != null) {
            entity.lifeWindTitle = weather.life.winds[0];
            entity.lifeWindContent = weather.life.winds[1];
        }
        if (weather.life.aqis != null) {
            entity.lifeAqiTitle = weather.life.aqis[0];
            entity.lifeAqiContent = weather.life.aqis[1];
        }
        if (weather.life.humidities != null) {
            entity.lifeHumidityTitle = weather.life.humidities[0];
            entity.lifeHumidityContent = weather.life.humidities[1];
        }
        if (weather.life.uvs != null) {
            entity.lifeUvTitle = weather.life.uvs[0];
            entity.lifeUvContent = weather.life.uvs[1];
        }
        if (weather.life.dresses != null) {
            entity.lifeDressesTitle = weather.life.dresses[0];
            entity.lifeDressesContent = weather.life.dresses[1];
        }
        if (weather.life.exercises != null) {
            entity.lifeExerciseTitle = weather.life.exercises[0];
            entity.lifeExerciseContent = weather.life.exercises[1];
        }
        if (weather.life.washCars != null) {
            entity.lifeWashCarTitle = weather.life.washCars[0];
            entity.lifeWashCarContent = weather.life.washCars[1];
        }
        if (weather.life.colds != null) {
            entity.lifeColdTitle = weather.life.colds[0];
            entity.lifeColdContent = weather.life.colds[1];
        }

        return entity;
    }

    /** <br> database. */

    // insert.

    public static void insertWeather(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        WeatherEntity entity = searchWeatherEntity(database, location);
        if (entity != null) {
            deleteWeather(database, entity);
        }
        new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao()
                .insert(buildWeatherEntity(weather));
    }

    // delete.

    private static void deleteWeather(SQLiteDatabase database, WeatherEntity entity) {
        new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao()
                .delete(entity);
    }

    // search.

    public static Weather searchWeather(SQLiteDatabase database, Location location) {
        WeatherEntity entity = searchWeatherEntity(database, location);
        if (entity == null) {
            return null;
        } else {
            return Weather.buildWeatherPrimaryData(entity);
        }
    }

    private static WeatherEntity searchWeatherEntity(SQLiteDatabase database, Location location) {
        WeatherEntityDao dao = new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao();

        List<WeatherEntity> entityList = dao
                .queryBuilder()
                .where(location.isEngLocation() ?
                        WeatherEntityDao.Properties.City.eq(location.city) : WeatherEntityDao.Properties.CityId.eq(location.cityId))
                .list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
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

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRealTimeWeather() {
        return this.realTimeWeather;
    }

    public void setRealTimeWeather(String realTimeWeather) {
        this.realTimeWeather = realTimeWeather;
    }

    public String getRealTimeWeatherKind() {
        return this.realTimeWeatherKind;
    }

    public void setRealTimeWeatherKind(String realTimeWeatherKind) {
        this.realTimeWeatherKind = realTimeWeatherKind;
    }

    public int getRealTimeTemp() {
        return this.realTimeTemp;
    }

    public void setRealTimeTemp(int realTimeTemp) {
        this.realTimeTemp = realTimeTemp;
    }

    public int getRealTimeSendibleTemp() {
        return this.realTimeSendibleTemp;
    }

    public void setRealTimeSendibleTemp(int realTimeSendibleTemp) {
        this.realTimeSendibleTemp = realTimeSendibleTemp;
    }

    public String getRealTimeWindDir() {
        return this.realTimeWindDir;
    }

    public void setRealTimeWindDir(String realTimeWindDir) {
        this.realTimeWindDir = realTimeWindDir;
    }

    public String getRealTimeWindLevel() {
        return this.realTimeWindLevel;
    }

    public void setRealTimeWindLevel(String realTimeWindLevel) {
        this.realTimeWindLevel = realTimeWindLevel;
    }

    public String getAqiAqi() {
        return this.aqiAqi;
    }

    public void setAqiAqi(String aqiAqi) {
        this.aqiAqi = aqiAqi;
    }

    public String getAqiRank() {
        return this.aqiRank;
    }

    public void setAqiRank(String aqiRank) {
        this.aqiRank = aqiRank;
    }

    public String getAqiPm25() {
        return this.aqiPm25;
    }

    public void setAqiPm25(String aqiPm25) {
        this.aqiPm25 = aqiPm25;
    }

    public String getAqiPm10() {
        return this.aqiPm10;
    }

    public void setAqiPm10(String aqiPm10) {
        this.aqiPm10 = aqiPm10;
    }

    public String getAqiQuality() {
        return this.aqiQuality;
    }

    public void setAqiQuality(String aqiQuality) {
        this.aqiQuality = aqiQuality;
    }

    public String getAqiDescription() {
        return this.aqiDescription;
    }

    public void setAqiDescription(String aqiDescription) {
        this.aqiDescription = aqiDescription;
    }

    public String getLifeWindTitle() {
        return this.lifeWindTitle;
    }

    public void setLifeWindTitle(String lifeWindTitle) {
        this.lifeWindTitle = lifeWindTitle;
    }

    public String getLifeWindContent() {
        return this.lifeWindContent;
    }

    public void setLifeWindContent(String lifeWindContent) {
        this.lifeWindContent = lifeWindContent;
    }

    public String getLifeAqiTitle() {
        return this.lifeAqiTitle;
    }

    public void setLifeAqiTitle(String lifeAqiTitle) {
        this.lifeAqiTitle = lifeAqiTitle;
    }

    public String getLifeAqiContent() {
        return this.lifeAqiContent;
    }

    public void setLifeAqiContent(String lifeAqiContent) {
        this.lifeAqiContent = lifeAqiContent;
    }

    public String getLifeHumidityTitle() {
        return this.lifeHumidityTitle;
    }

    public void setLifeHumidityTitle(String lifeHumidityTitle) {
        this.lifeHumidityTitle = lifeHumidityTitle;
    }

    public String getLifeHumidityContent() {
        return this.lifeHumidityContent;
    }

    public void setLifeHumidityContent(String lifeHumidityContent) {
        this.lifeHumidityContent = lifeHumidityContent;
    }

    public String getLifeUvTitle() {
        return this.lifeUvTitle;
    }

    public void setLifeUvTitle(String lifeUvTitle) {
        this.lifeUvTitle = lifeUvTitle;
    }

    public String getLifeUvContent() {
        return this.lifeUvContent;
    }

    public void setLifeUvContent(String lifeUvContent) {
        this.lifeUvContent = lifeUvContent;
    }

    public String getLifeDressesTitle() {
        return this.lifeDressesTitle;
    }

    public void setLifeDressesTitle(String lifeDressesTitle) {
        this.lifeDressesTitle = lifeDressesTitle;
    }

    public String getLifeDressesContent() {
        return this.lifeDressesContent;
    }

    public void setLifeDressesContent(String lifeDressesContent) {
        this.lifeDressesContent = lifeDressesContent;
    }

    public String getLifeExerciseTitle() {
        return this.lifeExerciseTitle;
    }

    public void setLifeExerciseTitle(String lifeExerciseTitle) {
        this.lifeExerciseTitle = lifeExerciseTitle;
    }

    public String getLifeExerciseContent() {
        return this.lifeExerciseContent;
    }

    public void setLifeExerciseContent(String lifeExerciseContent) {
        this.lifeExerciseContent = lifeExerciseContent;
    }

    public String getLifeWashCarTitle() {
        return this.lifeWashCarTitle;
    }

    public void setLifeWashCarTitle(String lifeWashCarTitle) {
        this.lifeWashCarTitle = lifeWashCarTitle;
    }

    public String getLifeWashCarContent() {
        return this.lifeWashCarContent;
    }

    public void setLifeWashCarContent(String lifeWashCarContent) {
        this.lifeWashCarContent = lifeWashCarContent;
    }

    public String getLifeColdTitle() {
        return this.lifeColdTitle;
    }

    public void setLifeColdTitle(String lifeColdTitle) {
        this.lifeColdTitle = lifeColdTitle;
    }

    public String getLifeColdContent() {
        return this.lifeColdContent;
    }

    public void setLifeColdContent(String lifeColdContent) {
        this.lifeColdContent = lifeColdContent;
    }


}

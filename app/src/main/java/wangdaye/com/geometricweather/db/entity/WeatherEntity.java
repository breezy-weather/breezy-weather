package wangdaye.com.geometricweather.db.entity;

import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.table.DaoMaster;
import wangdaye.com.geometricweather.db.entity.weather.WeatherEntityDao;

import org.greenrobot.greendao.annotation.Id;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Weather entity.
 * */

@Entity
public class WeatherEntity {
    // data
    @Id public Long id;
    public long timeStamp;

    // base.
    public String cityId;
    public String city;
    public String date;
    public String time;

    // realTime.
    public String realTimeWeather;
    public String realTimeWeatherKind;
    public int realTimeTemp;
    public int realTimeSensibleTemp;
    public String realTimeWindDir;
    public String realTimeWindSpeed;
    public String realTimeWindLevel;
    public int realTimeWindDegree;
    public String realTimeSimpleForecast;

    // aqi.
    public String aqiQuality;
    public int aqiAqi;
    public int aqiPm25;
    public int aqiPm10;
    public int aqiSo2;
    public int aqiNo2;
    public int aqiO3;
    public float aqiCo;

    //life.
    public String indexSimpleForecast;
    public String indexBriefing;
    public String indexCurrentWind;
    public String indexDailyWind;
    public String indexSensibleTemp;
    public String indexHumidity;
    public String indexUv;
    public String indexPressure;
    public String indexVisibility;
    public String indexDewPoint;

    @Generated(hash = 677751435)
    public WeatherEntity(Long id, long timeStamp, String cityId, String city, String date, String time,
            String realTimeWeather, String realTimeWeatherKind, int realTimeTemp, int realTimeSensibleTemp,
            String realTimeWindDir, String realTimeWindSpeed, String realTimeWindLevel, int realTimeWindDegree,
            String realTimeSimpleForecast, String aqiQuality, int aqiAqi, int aqiPm25, int aqiPm10, int aqiSo2,
            int aqiNo2, int aqiO3, float aqiCo, String indexSimpleForecast, String indexBriefing,
            String indexCurrentWind, String indexDailyWind, String indexSensibleTemp, String indexHumidity,
            String indexUv, String indexPressure, String indexVisibility, String indexDewPoint) {
        this.id = id;
        this.timeStamp = timeStamp;
        this.cityId = cityId;
        this.city = city;
        this.date = date;
        this.time = time;
        this.realTimeWeather = realTimeWeather;
        this.realTimeWeatherKind = realTimeWeatherKind;
        this.realTimeTemp = realTimeTemp;
        this.realTimeSensibleTemp = realTimeSensibleTemp;
        this.realTimeWindDir = realTimeWindDir;
        this.realTimeWindSpeed = realTimeWindSpeed;
        this.realTimeWindLevel = realTimeWindLevel;
        this.realTimeWindDegree = realTimeWindDegree;
        this.realTimeSimpleForecast = realTimeSimpleForecast;
        this.aqiQuality = aqiQuality;
        this.aqiAqi = aqiAqi;
        this.aqiPm25 = aqiPm25;
        this.aqiPm10 = aqiPm10;
        this.aqiSo2 = aqiSo2;
        this.aqiNo2 = aqiNo2;
        this.aqiO3 = aqiO3;
        this.aqiCo = aqiCo;
        this.indexSimpleForecast = indexSimpleForecast;
        this.indexBriefing = indexBriefing;
        this.indexCurrentWind = indexCurrentWind;
        this.indexDailyWind = indexDailyWind;
        this.indexSensibleTemp = indexSensibleTemp;
        this.indexHumidity = indexHumidity;
        this.indexUv = indexUv;
        this.indexPressure = indexPressure;
        this.indexVisibility = indexVisibility;
        this.indexDewPoint = indexDewPoint;
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
        entity.timeStamp = weather.base.timeStamp;

        // realTime.
        entity.realTimeWeather = weather.realTime.weather;
        entity.realTimeWeatherKind = weather.realTime.weatherKind;
        entity.realTimeTemp = weather.realTime.temp;
        entity.realTimeSensibleTemp = weather.realTime.sensibleTemp;
        entity.realTimeWindDir = weather.realTime.windDir;
        entity.realTimeWindSpeed = weather.realTime.windSpeed;
        entity.realTimeWindLevel = weather.realTime.windLevel;
        entity.realTimeWindDegree = weather.realTime.windDegree;
        entity.realTimeSimpleForecast = weather.realTime.simpleForecast;

        // aqi.
        entity.aqiQuality = weather.aqi.quality;
        entity.aqiAqi = weather.aqi.aqi;
        entity.aqiPm25 = weather.aqi.pm25;
        entity.aqiPm10 = weather.aqi.pm10;
        entity.aqiSo2 = weather.aqi.so2;
        entity.aqiNo2 = weather.aqi.no2;
        entity.aqiO3 = weather.aqi.o3;
        entity.aqiCo = weather.aqi.co;

        //life.
        entity.indexSimpleForecast = weather.index.simpleForecast;
        entity.indexBriefing = weather.index.briefing;
        entity.indexCurrentWind = weather.index.currentWind;
        entity.indexDailyWind = weather.index.dailyWind;
        entity.indexSensibleTemp = weather.index.sensibleTemp;
        entity.indexHumidity = weather.index.humidity;
        entity.indexUv = weather.index.uv;
        entity.indexPressure = weather.index.pressure;
        entity.indexVisibility = weather.index.visibility;
        entity.indexDewPoint = weather.index.dewPoint;

        return entity;
    }

    // insert.

    public static void insertWeather(SQLiteDatabase database, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        List<WeatherEntity> entityList = searchWeatherEntityList(database, location);
        for (int i = 0; i < entityList.size(); i ++) {
            deleteWeather(database, entityList.get(i));
        }
        new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao()
                .insert(buildWeatherEntity(weather));
    }

    // delete.

    public static void deleteWeather(SQLiteDatabase database, Location location) {
        List<WeatherEntity> entityList = searchWeatherEntityList(database, location);
        new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao()
                .deleteInTx(entityList);
    }

    private static void deleteWeather(SQLiteDatabase database, WeatherEntity entity) {
        new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao()
                .delete(entity);
    }

    // search.

    @Nullable
    public static WeatherEntity searchWeatherEntity(SQLiteDatabase database, Location location) {
        if (TextUtils.isEmpty(location.cityId)) {
            return null;
        }

        WeatherEntityDao dao = new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao();

        List<WeatherEntity> entityList = dao
                .queryBuilder()
                .where(WeatherEntityDao.Properties.CityId.eq(location.cityId))
                .list();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    private static List<WeatherEntity> searchWeatherEntityList(SQLiteDatabase database, Location location) {
        WeatherEntityDao dao = new DaoMaster(database)
                .newSession()
                .getWeatherEntityDao();
        return dao.queryBuilder()
                .where(WeatherEntityDao.Properties.CityId.eq(location.cityId))
                .list();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public int getRealTimeSensibleTemp() {
        return this.realTimeSensibleTemp;
    }

    public void setRealTimeSensibleTemp(int realTimeSensibleTemp) {
        this.realTimeSensibleTemp = realTimeSensibleTemp;
    }

    public String getRealTimeWindDir() {
        return this.realTimeWindDir;
    }

    public void setRealTimeWindDir(String realTimeWindDir) {
        this.realTimeWindDir = realTimeWindDir;
    }

    public String getRealTimeWindSpeed() {
        return this.realTimeWindSpeed;
    }

    public void setRealTimeWindSpeed(String realTimeWindSpeed) {
        this.realTimeWindSpeed = realTimeWindSpeed;
    }

    public String getRealTimeWindLevel() {
        return this.realTimeWindLevel;
    }

    public void setRealTimeWindLevel(String realTimeWindLevel) {
        this.realTimeWindLevel = realTimeWindLevel;
    }

    public int getRealTimeWindDegree() {
        return this.realTimeWindDegree;
    }

    public void setRealTimeWindDegree(int realTimeWindDegree) {
        this.realTimeWindDegree = realTimeWindDegree;
    }

    public String getRealTimeSimpleForecast() {
        return this.realTimeSimpleForecast;
    }

    public void setRealTimeSimpleForecast(String realTimeSimpleForecast) {
        this.realTimeSimpleForecast = realTimeSimpleForecast;
    }

    public String getAqiQuality() {
        return this.aqiQuality;
    }

    public void setAqiQuality(String aqiQuality) {
        this.aqiQuality = aqiQuality;
    }

    public int getAqiAqi() {
        return this.aqiAqi;
    }

    public void setAqiAqi(int aqiAqi) {
        this.aqiAqi = aqiAqi;
    }

    public int getAqiPm25() {
        return this.aqiPm25;
    }

    public void setAqiPm25(int aqiPm25) {
        this.aqiPm25 = aqiPm25;
    }

    public int getAqiPm10() {
        return this.aqiPm10;
    }

    public void setAqiPm10(int aqiPm10) {
        this.aqiPm10 = aqiPm10;
    }

    public int getAqiSo2() {
        return this.aqiSo2;
    }

    public void setAqiSo2(int aqiSo2) {
        this.aqiSo2 = aqiSo2;
    }

    public int getAqiNo2() {
        return this.aqiNo2;
    }

    public void setAqiNo2(int aqiNo2) {
        this.aqiNo2 = aqiNo2;
    }

    public int getAqiO3() {
        return this.aqiO3;
    }

    public void setAqiO3(int aqiO3) {
        this.aqiO3 = aqiO3;
    }

    public float getAqiCo() {
        return this.aqiCo;
    }

    public void setAqiCo(float aqiCo) {
        this.aqiCo = aqiCo;
    }

    public String getIndexSimpleForecast() {
        return this.indexSimpleForecast;
    }

    public void setIndexSimpleForecast(String indexSimpleForecast) {
        this.indexSimpleForecast = indexSimpleForecast;
    }

    public String getIndexBriefing() {
        return this.indexBriefing;
    }

    public void setIndexBriefing(String indexBriefing) {
        this.indexBriefing = indexBriefing;
    }

    public String getIndexCurrentWind() {
        return this.indexCurrentWind;
    }

    public void setIndexCurrentWind(String indexCurrentWind) {
        this.indexCurrentWind = indexCurrentWind;
    }

    public String getIndexDailyWind() {
        return this.indexDailyWind;
    }

    public void setIndexDailyWind(String indexDailyWind) {
        this.indexDailyWind = indexDailyWind;
    }

    public String getIndexSensibleTemp() {
        return this.indexSensibleTemp;
    }

    public void setIndexSensibleTemp(String indexSensibleTemp) {
        this.indexSensibleTemp = indexSensibleTemp;
    }

    public String getIndexHumidity() {
        return this.indexHumidity;
    }

    public void setIndexHumidity(String indexHumidity) {
        this.indexHumidity = indexHumidity;
    }

    public String getIndexUv() {
        return this.indexUv;
    }

    public void setIndexUv(String indexUv) {
        this.indexUv = indexUv;
    }

    public String getIndexPressure() {
        return this.indexPressure;
    }

    public void setIndexPressure(String indexPressure) {
        this.indexPressure = indexPressure;
    }

    public String getIndexVisibility() {
        return this.indexVisibility;
    }

    public void setIndexVisibility(String indexVisibility) {
        this.indexVisibility = indexVisibility;
    }

    public String getIndexDewPoint() {
        return this.indexDewPoint;
    }

    public void setIndexDewPoint(String indexDewPoint) {
        this.indexDewPoint = indexDewPoint;
    }
}

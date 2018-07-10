package wangdaye.com.geometricweather.data.entity.table.weather;

import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;

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
    public String aqiAqi;
    public String aqiPm25;
    public String aqiPm10;
    public String aqiQuality;

    //life.
    public String indexSimpleForecastTitle;
    public String indexSimpleForecastContent;
    public String indexBriefingTitle;
    public String indexBriefingContent;
    public String indexWindTitle;
    public String indexWindContent;
    public String indexAqiTitle;
    public String indexAqiContent;
    public String indexHumidityTitle;
    public String indexHumidityContent;
    public String indexUvTitle;
    public String indexUvContent;
    public String indexExerciseTitle;
    public String indexExerciseContent;
    public String indexColdTitle;
    public String indexColdContent;
    public String indexCarWashTitle;
    public String indexCarWashContent;

    @Generated(hash = 1155263832)
    public WeatherEntity(Long id, long timeStamp, String cityId, String city, String date, String time,
            String realTimeWeather, String realTimeWeatherKind, int realTimeTemp, int realTimeSensibleTemp,
            String realTimeWindDir, String realTimeWindSpeed, String realTimeWindLevel, int realTimeWindDegree,
            String realTimeSimpleForecast, String aqiAqi, String aqiPm25, String aqiPm10, String aqiQuality,
            String indexSimpleForecastTitle, String indexSimpleForecastContent, String indexBriefingTitle,
            String indexBriefingContent, String indexWindTitle, String indexWindContent, String indexAqiTitle,
            String indexAqiContent, String indexHumidityTitle, String indexHumidityContent, String indexUvTitle,
            String indexUvContent, String indexExerciseTitle, String indexExerciseContent, String indexColdTitle,
            String indexColdContent, String indexCarWashTitle, String indexCarWashContent) {
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
        this.aqiAqi = aqiAqi;
        this.aqiPm25 = aqiPm25;
        this.aqiPm10 = aqiPm10;
        this.aqiQuality = aqiQuality;
        this.indexSimpleForecastTitle = indexSimpleForecastTitle;
        this.indexSimpleForecastContent = indexSimpleForecastContent;
        this.indexBriefingTitle = indexBriefingTitle;
        this.indexBriefingContent = indexBriefingContent;
        this.indexWindTitle = indexWindTitle;
        this.indexWindContent = indexWindContent;
        this.indexAqiTitle = indexAqiTitle;
        this.indexAqiContent = indexAqiContent;
        this.indexHumidityTitle = indexHumidityTitle;
        this.indexHumidityContent = indexHumidityContent;
        this.indexUvTitle = indexUvTitle;
        this.indexUvContent = indexUvContent;
        this.indexExerciseTitle = indexExerciseTitle;
        this.indexExerciseContent = indexExerciseContent;
        this.indexColdTitle = indexColdTitle;
        this.indexColdContent = indexColdContent;
        this.indexCarWashTitle = indexCarWashTitle;
        this.indexCarWashContent = indexCarWashContent;
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
        entity.aqiAqi = weather.aqi.aqi;
        entity.aqiPm25 = weather.aqi.pm25;
        entity.aqiPm10 = weather.aqi.pm10;
        entity.aqiQuality = weather.aqi.quality;

        //life.
        entity.indexSimpleForecastTitle = weather.index.simpleForecasts[0];
        entity.indexSimpleForecastContent = weather.index.simpleForecasts[1];
        entity.indexBriefingTitle = weather.index.briefings[0];
        entity.indexBriefingContent = weather.index.briefings[1];
        entity.indexWindTitle = weather.index.winds[0];
        entity.indexWindContent = weather.index.winds[1];
        entity.indexAqiTitle = weather.index.aqis[0];
        entity.indexAqiContent = weather.index.aqis[1];
        entity.indexHumidityTitle = weather.index.humidities[0];
        entity.indexHumidityContent = weather.index.humidities[1];
        entity.indexUvTitle = weather.index.uvs[0];
        entity.indexUvContent = weather.index.uvs[1];
        entity.indexExerciseTitle = weather.index.exercises[0];
        entity.indexExerciseContent = weather.index.exercises[1];
        entity.indexColdTitle = weather.index.colds[0];
        entity.indexColdContent = weather.index.colds[1];
        entity.indexCarWashTitle = weather.index.carWashes[0];
        entity.indexCarWashContent = weather.index.carWashes[1];

        return entity;
    }

    /** <br> database. */

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

    public String getAqiAqi() {
        return this.aqiAqi;
    }

    public void setAqiAqi(String aqiAqi) {
        this.aqiAqi = aqiAqi;
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

    public String getIndexSimpleForecastTitle() {
        return this.indexSimpleForecastTitle;
    }

    public void setIndexSimpleForecastTitle(String indexSimpleForecastTitle) {
        this.indexSimpleForecastTitle = indexSimpleForecastTitle;
    }

    public String getIndexSimpleForecastContent() {
        return this.indexSimpleForecastContent;
    }

    public void setIndexSimpleForecastContent(String indexSimpleForecastContent) {
        this.indexSimpleForecastContent = indexSimpleForecastContent;
    }

    public String getIndexBriefingTitle() {
        return this.indexBriefingTitle;
    }

    public void setIndexBriefingTitle(String indexBriefingTitle) {
        this.indexBriefingTitle = indexBriefingTitle;
    }

    public String getIndexBriefingContent() {
        return this.indexBriefingContent;
    }

    public void setIndexBriefingContent(String indexBriefingContent) {
        this.indexBriefingContent = indexBriefingContent;
    }

    public String getIndexWindTitle() {
        return this.indexWindTitle;
    }

    public void setIndexWindTitle(String indexWindTitle) {
        this.indexWindTitle = indexWindTitle;
    }

    public String getIndexWindContent() {
        return this.indexWindContent;
    }

    public void setIndexWindContent(String indexWindContent) {
        this.indexWindContent = indexWindContent;
    }

    public String getIndexAqiTitle() {
        return this.indexAqiTitle;
    }

    public void setIndexAqiTitle(String indexAqiTitle) {
        this.indexAqiTitle = indexAqiTitle;
    }

    public String getIndexAqiContent() {
        return this.indexAqiContent;
    }

    public void setIndexAqiContent(String indexAqiContent) {
        this.indexAqiContent = indexAqiContent;
    }

    public String getIndexHumidityTitle() {
        return this.indexHumidityTitle;
    }

    public void setIndexHumidityTitle(String indexHumidityTitle) {
        this.indexHumidityTitle = indexHumidityTitle;
    }

    public String getIndexHumidityContent() {
        return this.indexHumidityContent;
    }

    public void setIndexHumidityContent(String indexHumidityContent) {
        this.indexHumidityContent = indexHumidityContent;
    }

    public String getIndexUvTitle() {
        return this.indexUvTitle;
    }

    public void setIndexUvTitle(String indexUvTitle) {
        this.indexUvTitle = indexUvTitle;
    }

    public String getIndexUvContent() {
        return this.indexUvContent;
    }

    public void setIndexUvContent(String indexUvContent) {
        this.indexUvContent = indexUvContent;
    }

    public String getIndexExerciseTitle() {
        return this.indexExerciseTitle;
    }

    public void setIndexExerciseTitle(String indexExerciseTitle) {
        this.indexExerciseTitle = indexExerciseTitle;
    }

    public String getIndexExerciseContent() {
        return this.indexExerciseContent;
    }

    public void setIndexExerciseContent(String indexExerciseContent) {
        this.indexExerciseContent = indexExerciseContent;
    }

    public String getIndexColdTitle() {
        return this.indexColdTitle;
    }

    public void setIndexColdTitle(String indexColdTitle) {
        this.indexColdTitle = indexColdTitle;
    }

    public String getIndexColdContent() {
        return this.indexColdContent;
    }

    public void setIndexColdContent(String indexColdContent) {
        this.indexColdContent = indexColdContent;
    }

    public String getIndexCarWashTitle() {
        return this.indexCarWashTitle;
    }

    public void setIndexCarWashTitle(String indexCarWashTitle) {
        this.indexCarWashTitle = indexCarWashTitle;
    }

    public String getIndexCarWashContent() {
        return this.indexCarWashContent;
    }

    public void setIndexCarWashContent(String indexCarWashContent) {
        this.indexCarWashContent = indexCarWashContent;
    }
}

package wangdaye.com.geometricweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.CNCity;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.basic.model.weather.Aqi;
import wangdaye.com.geometricweather.basic.model.weather.Base;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.Hourly;
import wangdaye.com.geometricweather.basic.model.weather.Index;
import wangdaye.com.geometricweather.basic.model.weather.RealTime;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.AlarmEntityDao;
import wangdaye.com.geometricweather.db.entity.CNCityEntity;
import wangdaye.com.geometricweather.db.entity.CNCityEntityDao;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoMaster;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;
import wangdaye.com.geometricweather.db.entity.HourlyEntityDao;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.AlarmEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.entity.HourlyEntity;
import wangdaye.com.geometricweather.db.entity.LocationEntityDao;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntityDao;

/**
 * Database helper
 * */

public class DatabaseHelper {

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }

    private GeoWeatherOpenHelper helper;

    private boolean writingCityList;
    private final Object writingLock = new Object();

    private final static String DATABASE_NAME = "Geometric_Weather_db";

    private class GeoWeatherOpenHelper extends DaoMaster.DevOpenHelper {

        private Context context;

        GeoWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
            this.context = context;
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            if (oldVersion < 30) {
                super.onUpgrade(db, oldVersion, newVersion);
                return;
            }
            if (newVersion >= 39 && oldVersion < 42) {
                CNCityEntityDao.dropTable(db, true);
                CNCityEntityDao.createTable(db, true);
            }
            if (newVersion >= 35 && oldVersion < 39) {
                WeatherEntityDao.dropTable(db, true);
                WeatherEntityDao.createTable(db, true);
                DailyEntityDao.dropTable(db, true);
                DailyEntityDao.createTable(db, true);
                HourlyEntityDao.dropTable(db, true);
                HourlyEntityDao.createTable(db, true);
                AlarmEntityDao.dropTable(db, true);
                AlarmEntityDao.createTable(db, true);
            }
            if (newVersion >= 30 && oldVersion < 34) {
                CNCityEntityDao.dropTable(db, true);
                CNCityEntityDao.createTable(db, true);
                LocationEntityDao.dropTable(db, true);
                LocationEntityDao.createTable(db, true);
                Toast.makeText(
                        context,
                        context.getString(R.string.feedback_readd_location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private DatabaseHelper(Context c) {
        helper = new GeoWeatherOpenHelper(c, DATABASE_NAME, null);
        writingCityList = false;
    }

    private SQLiteDatabase getDatabase() {
        return helper.getWritableDatabase();
    }

    // location.

    public void writeLocation(Location location) {
        LocationEntity.insertLocation(getDatabase(), location);
    }

    public void writeLocationList(List<Location> list) {
        LocationEntity.writeLocationList(getDatabase(), list);
    }

    public void deleteLocation(Location location) {
        LocationEntity.deleteLocation(getDatabase(), location);
    }

    public List<Location> readLocationList() {
        return LocationEntity.readLocationList(getDatabase());
    }

    // history.

    public void writeTodayHistory(Weather weather) {
        HistoryEntity.insertTodayHistory(getDatabase(), weather);
    }

    public void writeYesterdayHistory(History history) {
        HistoryEntity.insertYesterdayHistory(getDatabase(), history);
    }

    public History readHistory(Weather weather) {
        return HistoryEntity.searchYesterdayHistory(getDatabase(), weather);
    }

    // weather.

    public void writeWeather(Location location, Weather weather) {
        WeatherEntity.insertWeather(getDatabase(), location, weather);
        DailyEntity.insertDailyList(getDatabase(), location, weather);
        HourlyEntity.insertDailyList(getDatabase(), location, weather);
        AlarmEntity.insertAlarmList(getDatabase(), location, weather);
    }

    public Weather readWeather(Location location) {
        WeatherEntity weatherEntity = WeatherEntity.searchWeatherEntity(getDatabase(), location);
        List<DailyEntity> dailyEntityList = DailyEntity.searchLocationDailyEntity(getDatabase(), location);
        List<HourlyEntity> hourlyEntityList = HourlyEntity.searchLocationHourlyEntity(getDatabase(), location);
        List<AlarmEntity> alarmEntityList = AlarmEntity.searchLocationAlarmEntity(getDatabase(), location);

        if (weatherEntity != null) {
            Base base = new Base(
                    weatherEntity.cityId, weatherEntity.city,
                    weatherEntity.date, weatherEntity.time, weatherEntity.timeStamp);
            RealTime realTime = new RealTime(
                    weatherEntity.realTimeWeather, weatherEntity.realTimeWeatherKind,
                    weatherEntity.realTimeTemp, weatherEntity.realTimeSensibleTemp,
                    weatherEntity.realTimeWindDir, weatherEntity.realTimeWindSpeed,
                    weatherEntity.realTimeWindLevel, weatherEntity.realTimeWindDegree,
                    weatherEntity.realTimeSimpleForecast);
            List<Daily> dailyList = new ArrayList<>();
            for (DailyEntity dailyEntity : dailyEntityList) {
                dailyList.add(
                        new Daily(
                                dailyEntity.date, dailyEntity.week,
                                new String[] {dailyEntity.daytimeWeather, dailyEntity.nighttimeWeather},
                                new String[] {dailyEntity.daytimeWeatherKind, dailyEntity.nighttimeWeatherKind},
                                new int[] {dailyEntity.maxiTemp, dailyEntity.miniTemp},
                                new String[] {dailyEntity.daytimeWindDir, dailyEntity.nighttimeWindDir},
                                new String[] {dailyEntity.daytimeWindSpeed, dailyEntity.nighttimeWindSpeed},
                                new String[] {dailyEntity.daytimeWindLevel, dailyEntity.nighttimeWindLevel},
                                new int[] {dailyEntity.daytimeWindDegree, dailyEntity.nighttimeWindDegree},
                                new String[] {dailyEntity.sunrise, dailyEntity.sunset, dailyEntity.moonrise, dailyEntity.moonset},
                                dailyEntity.moonPhase,
                                new int[] {dailyEntity.daytimePrecipitations, dailyEntity.nighttimePrecipitations}));
            }
            List<Hourly> hourlyList = new ArrayList<>();
            for (HourlyEntity hourlyEntity : hourlyEntityList) {
                hourlyList.add(
                        new Hourly(
                                hourlyEntity.time,
                                hourlyEntity.dayTime,
                                hourlyEntity.weather,
                                hourlyEntity.weatherKind,
                                hourlyEntity.temp,
                                hourlyEntity.precipitation));
            }
            Aqi aqi = new Aqi(
                    weatherEntity.aqiQuality, weatherEntity.aqiAqi, weatherEntity.aqiPm25,
                    weatherEntity.aqiPm10, weatherEntity.aqiSo2, weatherEntity.aqiNo2,
                    weatherEntity.aqiO3, weatherEntity.aqiCo);
            Index index = new Index(
                    weatherEntity.indexSimpleForecast, weatherEntity.indexBriefing,
                    weatherEntity.indexCurrentWind, weatherEntity.indexDailyWind,
                    weatherEntity.indexSensibleTemp, weatherEntity.indexHumidity, weatherEntity.indexUv,
                    weatherEntity.indexPressure, weatherEntity.indexVisibility, weatherEntity.indexDewPoint);
            List<Alert> alertList = new ArrayList<>();
            for (AlarmEntity alarmEntity : alarmEntityList) {
                alertList.add(
                        new Alert(
                                alarmEntity.alertId,
                                alarmEntity.description,
                                alarmEntity.content,
                                alarmEntity.publishTime));
            }

            return new Weather(base, realTime, dailyList, hourlyList, aqi, index, alertList);
        } else {
            return null;
        }
    }

    public void deleteWeather(Location location) {
        WeatherEntity.deleteWeather(getDatabase(), location);
        DailyEntity.deleteDailyList(getDatabase(), location);
        HourlyEntity.deleteHourlyList(getDatabase(), location);
        AlarmEntity.deleteAlarmList(getDatabase(), location);
        HistoryEntity.clearLocationHistory(getDatabase(), location);
    }

    // cn city.

    public void writeCityList(List<CNCity> list) {
        if (!writingCityList) {
            synchronized (writingLock) {
                if (!writingCityList) {
                    writingCityList = true;
                    CNCityEntity.removeCNCityList(getDatabase());
                    CNCityEntity.insertCNCityList(getDatabase(), list);
                    writingCityList = false;
                }
            }
        }
    }

    public CNCity readCNCity(String name) {
        return CNCityEntity.searchCNCity(getDatabase(), name);
    }

    public CNCity readCNCity(String district, String city, String province) {
        return CNCityEntity.searchCNCity(getDatabase(), district, city, province);
    }

    public List<CNCity> fuzzyReadCNCity(String name) {
        return CNCityEntity.fuzzySearchCNCity(getDatabase(), name);
    }

    public long countCNCity() {
        return CNCityEntity.countCNCity(getDatabase());
    }
}

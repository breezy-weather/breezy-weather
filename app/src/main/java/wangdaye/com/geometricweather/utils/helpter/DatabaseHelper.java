package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.CNCityList;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.table.CNCityEntity;
import wangdaye.com.geometricweather.data.entity.table.CNCityEntityDao;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;
import wangdaye.com.geometricweather.data.entity.table.HistoryEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntityDao;

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
        Weather weather = WeatherEntity.searchWeather(getDatabase(), location);
        if (weather != null) {
            weather
                    .buildWeatherDailyList(DailyEntity.searchLocationDailyEntity(getDatabase(), location))
                    .buildWeatherHourlyList(HourlyEntity.searchLocationHourlyEntity(getDatabase(), location))
                    .buildWeatherAlarmList(AlarmEntity.searchLocationAlarmEntity(getDatabase(), location));
        }
        return weather;
    }

    public void deleteWeather(Location location) {
        WeatherEntity.deleteWeather(getDatabase(), location);
        DailyEntity.deleteDailyList(getDatabase(), location);
        HourlyEntity.deleteHourlyList(getDatabase(), location);
        AlarmEntity.deleteAlarmList(getDatabase(), location);
        HistoryEntity.clearLocationHistory(getDatabase(), location);
    }

    // cn city.

    public void writeCityList(CNCityList list) {
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

    public CNCityList.CNCity readCNCity(String name) {
        return CNCityEntity.searchCNCity(getDatabase(), name);
    }

    public CNCityList.CNCity readCNCity(String district, String city, String province) {
        return CNCityEntity.searchCNCity(getDatabase(), district, city, province);
    }

    public List<CNCityList.CNCity> fuzzyReadCNCity(String name) {
        return CNCityEntity.fuzzySearchCNCity(getDatabase(), name);
    }

    public long countCNCity() {
        return CNCityEntity.countCNCity(getDatabase());
    }
}

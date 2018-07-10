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
import wangdaye.com.geometricweather.data.entity.table.HistoryEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntityDao;
import wangdaye.com.geometricweather.data.entity.table.weather.DaoMaster;
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
            if (newVersion >= 27 && oldVersion < 29) {
                // delete locations and guide user to re-add them in version 27 - 28.
                LocationEntityDao.dropTable(db, true);
                LocationEntityDao.createTable(db, true);
                Toast.makeText(
                        context,
                        context.getString(R.string.feedback_readd_location),
                        Toast.LENGTH_SHORT).show();
            }
            if (newVersion >= 24 && oldVersion < 26) {
                // added and modified cn city entity in version 24 - 26.
                CNCityEntityDao.dropTable(db, true);
                CNCityEntityDao.createTable(db, true);
            }
            if (newVersion >= 22) {
                // rebuild all entities in version 22 and version 23.
                if (oldVersion < 23) {
                    WeatherEntityDao.dropTable(db, true);
                    DailyEntityDao.dropTable(db, true);
                    HourlyEntityDao.dropTable(db, true);
                    AlarmEntityDao.dropTable(db, true);

                    WeatherEntityDao.createTable(db, true);
                    DailyEntityDao.createTable(db, true);
                    HourlyEntityDao.createTable(db, true);
                    AlarmEntityDao.createTable(db, true);
                }
            } else {
                super.onUpgrade(db, oldVersion, newVersion);
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

    public void clearLocation() {
        LocationEntity.clearLocation(getDatabase());
    }

    public List<Location> readLocationList() {
        return LocationEntity.readLocationList(getDatabase());
    }

    // history.

    public void writeHistory(Weather weather) {
        HistoryEntity.insertHistory(getDatabase(), weather);
    }

    public void writeHistory(History history) {
        HistoryEntity.insertHistory(getDatabase(), history);
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

    public CNCityList.CNCity readCNCity(String name, String province) {
        return CNCityEntity.searchCNCity(getDatabase(), name, province);
    }

    public List<CNCityList.CNCity> fuzzyReadCNCity(String name) {
        return CNCityEntity.fuzzySearchCNCity(getDatabase(), name);
    }

    public long countCNCity() {
        return CNCityEntity.countCNCity(getDatabase());
    }
/*
    // city.

    public void writeCityList(CityListResult result) {
        CityEntity.insertCityList(getDatabase(), result);
    }

    public boolean isNeedWriteCityList() {
        return CityEntity.isNeedWriteData(getDatabase());
    }

    public List<Location> readCityList() {
        return CityEntity.readCityLocation(getDatabase());
    }

    String[] searchCityId(String district, String city, String province) {
        List<Location> locationList = CityEntity.accurateSearchCity(getDatabase(), district);
        if (locationList.size() == 1) {
            return new String[] {
                    locationList.get(0).cityId,
                    district};
        } else if (locationList.size() == 0) {
            locationList = CityEntity.accurateSearchCity(getDatabase(), city);
            if (locationList.size() == 0) {
                return new String[] {Location.NULL_ID, ""};
            } else {
                return new String[] {
                        locationList.get(0).cityId,
                        city};
            }
        } else {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).prov.equals(province.replace("省", ""))) {
                    return new String[] {
                            locationList.get(i).cityId,
                            city
                    };
                }
            }
            return new String[] {Location.NULL_ID, ""};
        }
    }

    public List<Location> fuzzySearchCityList(String txt) {
        return CityEntity.fuzzySearchCity(getDatabase(), txt);
    }

    // oversea city.

    public void writeOverseaCityList(OverseaCityListResult result) {
        OverseaCityEntity.insertOverseaCityList(getDatabase(), result);
    }

    public boolean isNeedWriteOverseaCityList() {
        return OverseaCityEntity.isNeedWriteData(getDatabase());
    }

    public List<Location> readOverseaCityList() {
        return OverseaCityEntity.readOverseaCityLocation(getDatabase());
    }

    String[] searchOverseaCityId(String city) {
        List<Location> locationList = OverseaCityEntity.accurateSearchOverseaCity(getDatabase(), city);
        if (locationList.size() > 0) {
            return new String[] {
                    locationList.get(0).cityId,
                    city};
        } else {
            return new String[] {Location.NULL_ID, ""};
        }
    }

    public List<Location> fuzzySearchOverseaCityList(String txt) {
        return OverseaCityEntity.fuzzySearchOverseaCity(getDatabase(), txt);
    }
*/
}

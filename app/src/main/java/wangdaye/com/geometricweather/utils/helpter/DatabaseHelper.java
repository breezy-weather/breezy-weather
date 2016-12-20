package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.data.entity.result.CityListResult;
import wangdaye.com.geometricweather.data.entity.result.OverseaCityListResult;
import wangdaye.com.geometricweather.data.entity.table.CityEntity;
import wangdaye.com.geometricweather.data.entity.table.DaoMaster;
import wangdaye.com.geometricweather.data.entity.table.HistoryEntity;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;
import wangdaye.com.geometricweather.data.entity.table.OverseaCityEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.utils.SafeHandler;

/**
 * Database helper
 * */

public class DatabaseHelper {
    // data
    private DaoMaster.DevOpenHelper helper;
    private final static String DATABASE_NAME = "Geometric_Weather_db";

    /** <br> life cycle. */

    private DatabaseHelper(Context c) {
        helper = new DaoMaster.DevOpenHelper(c, DATABASE_NAME);
    }

    /** <br> data. */

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

    public void writeHistory(Weather weather) {
        HistoryEntity.insertHistory(getDatabase(), weather);
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

    // city.

    public void writeCityList(SafeHandler handler, CityListResult result) {
        CityEntity.insertCityList(getDatabase(), handler, result);
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

    public void writeOverseaCityList(SafeHandler handler, OverseaCityListResult result) {
        OverseaCityEntity.insertOverseaCityList(getDatabase(), handler, result);
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

    /** <br> singleton. */

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }
}

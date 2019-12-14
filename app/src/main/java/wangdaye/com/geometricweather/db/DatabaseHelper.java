package wangdaye.com.geometricweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.greendao.DbUtils;
import org.greenrobot.greendao.database.Database;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.ChineseCity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.History;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.controller.AlertEntityController;
import wangdaye.com.geometricweather.db.controller.ChineseCityEntityController;
import wangdaye.com.geometricweather.db.controller.DailyEntityController;
import wangdaye.com.geometricweather.db.controller.HistoryEntityController;
import wangdaye.com.geometricweather.db.controller.HourlyEntityController;
import wangdaye.com.geometricweather.db.controller.LocationEntityController;
import wangdaye.com.geometricweather.db.controller.MinutelyEntityController;
import wangdaye.com.geometricweather.db.controller.WeatherEntityController;
import wangdaye.com.geometricweather.db.converter.AlertEntityConverter;
import wangdaye.com.geometricweather.db.converter.ChineseCityEntityConverter;
import wangdaye.com.geometricweather.db.converter.DailyEntityConverter;
import wangdaye.com.geometricweather.db.converter.HistoryEntityConverter;
import wangdaye.com.geometricweather.db.converter.HourlyEntityConverter;
import wangdaye.com.geometricweather.db.converter.LocationEntityConverter;
import wangdaye.com.geometricweather.db.converter.MinutelyEntityConverter;
import wangdaye.com.geometricweather.db.converter.WeatherEntityConverter;
import wangdaye.com.geometricweather.db.entity.AlertEntityDao;
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoMaster;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;
import wangdaye.com.geometricweather.db.entity.HistoryEntityDao;
import wangdaye.com.geometricweather.db.entity.HourlyEntityDao;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.MinutelyEntityDao;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntityDao;

/**
 * Database helper
 * */

public class DatabaseHelper {

    private static volatile DatabaseHelper instance;
    public static DatabaseHelper getInstance(Context c) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                instance = new DatabaseHelper(c);
            }
        }
        return instance;
    }

    private DatabaseOpenHelper openHelper;

    private LocationEntityController locationEntityController;
    private ChineseCityEntityController chineseCityEntityController;
    private WeatherEntityController weatherEntityController;
    private DailyEntityController dailyEntityController;
    private HourlyEntityController hourlyEntityController;
    private MinutelyEntityController minutelyEntityController;
    private AlertEntityController alertEntityController;
    private HistoryEntityController historyEntityController;

    private boolean writingCityList;
    private final Object writingLock;

    private final static String DATABASE_NAME = "Geometric_Weather_db";

    private DatabaseHelper(Context c) {
        openHelper = new DatabaseOpenHelper(c, DATABASE_NAME, null);

        DaoSession session = new DaoMaster(openHelper.getWritableDatabase()).newSession();
        locationEntityController = new LocationEntityController(session);
        chineseCityEntityController = new ChineseCityEntityController(session);
        weatherEntityController = new WeatherEntityController(session);
        dailyEntityController = new DailyEntityController(session);
        hourlyEntityController = new HourlyEntityController(session);
        minutelyEntityController = new MinutelyEntityController(session);
        alertEntityController = new AlertEntityController(session);
        historyEntityController = new HistoryEntityController(session);

        writingCityList = false;
        writingLock = new Object();
    }

    // location.

    public void writeLocation(@NonNull Location location) {
        LocationEntity entity = locationEntityController.selectLocationEntity(location.getFormattedId());
        if (entity == null) {
            writeLocation(location, locationEntityController.countLocationEntity() + 1);
        } else {
            writeLocation(location, entity.sequence);
        }
    }

    public void writeLocation(@NonNull Location location, long sequence) {
        locationEntityController.insertOrUpdateLocationEntity(
                LocationEntityConverter.convertToEntity(location, sequence));
    }

    public void writeLocationList(@NonNull List<Location> list) {
        locationEntityController.insertOrUpdateLocationEntityList(
                LocationEntityConverter.convertToEntityList(list));
    }

    public void deleteLocation(@NonNull Location location) {
        locationEntityController.deleteLocationEntity(
                LocationEntityConverter.convertToEntity(location, 0));
    }

    @Nullable
    public Location readLocation(@NonNull Location location) {
        return readLocation(location.getFormattedId());
    }

    @Nullable
    public Location readLocation(@NonNull String formattedId) {
        LocationEntity entity = locationEntityController.selectLocationEntity(formattedId);
        if (entity != null) {
            return LocationEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<Location> readLocationList() {
        List<LocationEntity> entityList = locationEntityController.selectLocationEntityList();
        if (entityList.size() == 0) {
            entityList.add(
                    LocationEntityConverter.convertToEntity(Location.buildLocal(), 0)
            );
            locationEntityController.insertOrUpdateLocationEntityList(entityList);
        }
        return LocationEntityConverter.convertToModuleList(entityList);
    }

    public int countLocation() {
        return locationEntityController.countLocationEntity();
    }

    // weather.

    public void writeWeather(@NonNull Location location, @NonNull Weather weather) {
        weatherEntityController.insertWeatherEntity(
                location.getCityId(),
                location.getWeatherSource(),
                WeatherEntityConverter.convert(location, weather)
        );
        dailyEntityController.insertDailyList(
                location.getCityId(),
                location.getWeatherSource(),
                DailyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getDailyForecast()
                )
        );
        hourlyEntityController.insertHourlyList(
                location.getCityId(),
                location.getWeatherSource(),
                HourlyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getHourlyForecast()
                )
        );
        minutelyEntityController.insertMinutelyList(
                location.getCityId(),
                location.getWeatherSource(),
                MinutelyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getMinutelyForecast()
                )
        );
        alertEntityController.insertAlertList(
                location.getCityId(),
                location.getWeatherSource(),
                AlertEntityConverter.convertToEntityList(
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getAlertList()
                )
        );
        writeTodayHistory(location, weather);
        if (weather.getYesterday() != null) {
            writeYesterdayHistory(location, weather, weather.getYesterday());
        }
    }

    @Nullable
    public Weather readWeather(@NonNull Location location) {
        WeatherEntity weatherEntity = weatherEntityController.selectWeatherEntity(
                location.getCityId(), location.getWeatherSource());
        if (weatherEntity == null) {
            return null;
        }

        HistoryEntity historyEntity = historyEntityController.selectYesterdayHistoryEntity(
                location.getCityId(), location.getWeatherSource(),weatherEntity.updateDate);
        return WeatherEntityConverter.convert(weatherEntity, historyEntity);
    }

    public void deleteWeather(@NonNull Location location) {
        weatherEntityController.deleteWeather(location.getCityId(), location.getWeatherSource());
        historyEntityController.deleteLocationHistoryEntity(location.getCityId(), location.getWeatherSource());
        dailyEntityController.deleteDailyEntityList(location.getCityId(), location.getWeatherSource());
        hourlyEntityController.deleteHourlyEntityList(location.getCityId(), location.getWeatherSource());
        minutelyEntityController.deleteMinutelyEntityList(location.getCityId(), location.getWeatherSource());
        alertEntityController.deleteAlertList(location.getCityId(), location.getWeatherSource());

        DbUtils.vacuum(new DaoMaster(openHelper.getWritableDatabase()).getDatabase());
    }

    // history.

    public void writeTodayHistory(@NonNull Location location, @NonNull Weather weather) {
        historyEntityController.insertTodayHistoryEntity(
                location.getCityId(),
                location.getWeatherSource(),
                weather.getBase().getPublishDate(),
                HistoryEntityConverter.convert(location.getCityId(), location.getWeatherSource(), weather)
        );
    }

    public void writeYesterdayHistory(@NonNull Location location,
                                      @NonNull Weather weather, @NonNull History history) {
        historyEntityController.insertYesterdayHistoryEntity(
                location.getCityId(),
                location.getWeatherSource(),
                weather.getBase().getPublishDate(),
                HistoryEntityConverter.convert(location.getCityId(), location.getWeatherSource(), history)
        );
    }

    public History readHistory(@NonNull Location location, @NonNull Weather weather) {
        return HistoryEntityConverter.convert(
                historyEntityController.selectYesterdayHistoryEntity(
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getBase().getPublishDate()
                )
        );
    }

    // chinese city.

    public void writeChineseCityList(@NonNull List<ChineseCity> list) {
        if (!writingCityList) {
            synchronized (writingLock) {
                if (!writingCityList) {
                    writingCityList = true;
                    chineseCityEntityController.deleteChineseCityEntityList();
                    chineseCityEntityController.insertChineseCityEntityList(
                            ChineseCityEntityConverter.convertToEntityList(list));
                    writingCityList = false;
                }
            }
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String name) {
        ChineseCityEntity entity = chineseCityEntityController.selectChineseCityEntity(name);
        if (entity != null) {
            return ChineseCityEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String province,
                                       @NonNull String city,
                                       @NonNull String district) {
        ChineseCityEntity entity = chineseCityEntityController.selectChineseCityEntity(
                province, city, district);
        if (entity != null) {
            return ChineseCityEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(float latitude, float longitude) {
        ChineseCityEntity entity = chineseCityEntityController.selectChineseCityEntity(
                latitude, longitude);
        if (entity != null) {
            return ChineseCityEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<ChineseCity> readChineseCityList(@NonNull String name) {
        return ChineseCityEntityConverter.convertToModuleList(
                chineseCityEntityController.selectChineseCityEntityList(name));
    }

    public int countChineseCity() {
        return chineseCityEntityController.countChineseCityEntity();
    }
}

class DatabaseOpenHelper extends DaoMaster.DevOpenHelper {

    DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 52:
            case 51:
            case 50:
            case 49:
            case 48:
            case 47:
                WeatherEntityDao.dropTable(db, true);
                DailyEntityDao.dropTable(db, true);
                HourlyEntityDao.dropTable(db, true);
                MinutelyEntityDao.dropTable(db, true);
                AlertEntityDao.dropTable(db, true);
                HistoryEntityDao.dropTable(db, true);

                WeatherEntityDao.createTable(db, true);
                DailyEntityDao.createTable(db, true);
                HourlyEntityDao.createTable(db, true);
                MinutelyEntityDao.createTable(db, true);
                AlertEntityDao.createTable(db, true);
                HistoryEntityDao.createTable(db, true);
                break;

            default:
                super.onUpgrade(db, oldVersion, newVersion);
                break;
        }
    }
}

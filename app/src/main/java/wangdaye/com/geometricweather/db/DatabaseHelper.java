package wangdaye.com.geometricweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.greendao.DbUtils;
import org.greenrobot.greendao.database.Database;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.ChineseCity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.weather.History;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.controller.AlertEntityController;
import wangdaye.com.geometricweather.db.controller.ChineseCityEntityController;
import wangdaye.com.geometricweather.db.controller.DailyEntitiyController;
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
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entity.DaoMaster;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;

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
    private DailyEntitiyController dailyEntitiyController;
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
        dailyEntitiyController = new DailyEntitiyController(session);
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
        LocationEntity entity = locationEntityController.selectLocationEntity(location.getFormattedId());
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
                WeatherEntityConverter.convert(weather)
        );
        dailyEntitiyController.insertDailyList(
                location.getCityId(),
                DailyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        weather.getDailyForecast()
                )
        );
        hourlyEntityController.insertHourlyList(
                location.getCityId(),
                HourlyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        weather.getHourlyForecast()
                )
        );
        minutelyEntityController.insertMinutelyList(
                location.getCityId(),
                MinutelyEntityConverter.convertToEntityList(
                        location.getCityId(),
                        weather.getMinutelyForecast()
                )
        );
        alertEntityController.insertAlertList(
                location.getCityId(),
                AlertEntityConverter.convertToEntityList(
                        location.getCityId(),
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
        WeatherEntity weatherEntity = weatherEntityController.selectWeatherEntity(location.getCityId());
        if (weatherEntity == null) {
            return null;
        }

        HistoryEntity historyEntity = historyEntityController.selectYesterdayHistoryEntity(
                location.getCityId(), weatherEntity.updateDate);
        return WeatherEntityConverter.convert(weatherEntity, historyEntity);
    }

    public void deleteWeather(@NonNull Location location) {
        weatherEntityController.deleteWeather(location.getCityId());
        historyEntityController.deleteLocationHistoryEntity(location.getCityId());
        dailyEntitiyController.deleteDailyEntityList(location.getCityId());
        hourlyEntityController.deleteHourlyEntityList(location.getCityId());
        minutelyEntityController.deleteMinutelyEntityList(location.getCityId());
        alertEntityController.deleteAlertList(location.getCityId());

        DbUtils.vacuum(new DaoMaster(openHelper.getWritableDatabase()).getDatabase());
    }

    // history.

    public void writeTodayHistory(@NonNull Location location, @NonNull Weather weather) {
        historyEntityController.insertTodayHistoryEntity(
                location.getCityId(),
                weather.getBase().getPublishDate(),
                HistoryEntityConverter.convert(location.getCityId(), weather)
        );
    }

    public void writeYesterdayHistory(@NonNull Location location,
                                      @NonNull Weather weather, @NonNull History history) {
        historyEntityController.insertYesterdayHistoryEntity(
                location.getCityId(),
                weather.getBase().getPublishDate(),
                HistoryEntityConverter.convert(location.getCityId(), history)
        );
    }

    public History readHistory(@NonNull Location location, @NonNull Weather weather) {
        return HistoryEntityConverter.convert(
                historyEntityController.selectYesterdayHistoryEntity(
                        location.getCityId(),
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

    private Context context;

    DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context = context;
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Toast.makeText(
                context,
                context.getString(R.string.feedback_readd_location),
                Toast.LENGTH_SHORT).show();
    }
}

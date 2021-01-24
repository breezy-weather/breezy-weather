package wangdaye.com.geometricweather.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.greendao.DbUtils;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.ChineseCity;
import wangdaye.com.geometricweather.basic.model.Location;
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
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entity.DaoMaster;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;
import wangdaye.com.geometricweather.utils.FileUtils;

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

    private final DatabaseOpenHelper openHelper;
    private final DaoSession session;

    private final LocationEntityController locationEntityController;
    private final ChineseCityEntityController chineseCityEntityController;
    private final WeatherEntityController weatherEntityController;
    private final DailyEntityController dailyEntityController;
    private final HourlyEntityController hourlyEntityController;
    private final MinutelyEntityController minutelyEntityController;
    private final AlertEntityController alertEntityController;
    private final HistoryEntityController historyEntityController;

    private final Object writingLock;

    private final static String DATABASE_NAME = "Geometric_Weather_db";

    private DatabaseHelper(Context c) {
        openHelper = new DatabaseOpenHelper(c, DATABASE_NAME, null);
        session = new DaoMaster(openHelper.getWritableDatabase()).newSession();

        locationEntityController = new LocationEntityController();
        chineseCityEntityController = new ChineseCityEntityController();
        weatherEntityController = new WeatherEntityController();
        dailyEntityController = new DailyEntityController();
        hourlyEntityController = new HourlyEntityController();
        minutelyEntityController = new MinutelyEntityController();
        alertEntityController = new AlertEntityController();
        historyEntityController = new HistoryEntityController();

        writingLock = new Object();
    }

    // location.

    public void writeLocation(@NonNull Location location) {
        session.callInTxNoException(() -> {
            LocationEntity entity = LocationEntityConverter.convertToEntity(location);
            if (locationEntityController.selectLocationEntity(session, location.getFormattedId()) == null) {
                locationEntityController.insertLocationEntity(session, entity);
            } else {
                locationEntityController.updateLocationEntity(session, entity);
            }
            return true;
        });
    }

    public void writeLocationList(@NonNull List<Location> list) {
        session.callInTxNoException(() -> {
            locationEntityController.deleteLocationEntityList(session);
            locationEntityController.insertLocationEntityList(
                    session,
                    LocationEntityConverter.convertToEntityList(list)
            );
            return true;
        });
    }

    public void deleteLocation(@NonNull Location location) {
        locationEntityController.deleteLocationEntity(
                session, LocationEntityConverter.convertToEntity(location));
    }

    @Nullable
    public Location readLocation(@NonNull Location location) {
        return readLocation(location.getFormattedId());
    }

    @Nullable
    public Location readLocation(@NonNull String formattedId) {
        LocationEntity entity = locationEntityController.selectLocationEntity(session, formattedId);
        if (entity != null) {
            return LocationEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<Location> readLocationList() {
        List<LocationEntity> entityList = locationEntityController.selectLocationEntityList(session);

        if (entityList.size() == 0) {
            synchronized (writingLock) {
                if (countLocation() == 0) {
                    LocationEntity entity = LocationEntityConverter.convertToEntity(
                            Location.buildLocal());
                    entityList.add(entity);

                    locationEntityController.insertLocationEntityList(session, entityList);

                    return LocationEntityConverter.convertToModuleList(entityList);
                }
            }
        }


        return LocationEntityConverter.convertToModuleList(entityList);
    }

    public int countLocation() {
        return locationEntityController.countLocationEntity(session);
    }

    // weather.

    public void writeWeather(@NonNull Location location, @NonNull Weather weather) {
        session.callInTxNoException(() -> {
            weatherEntityController.deleteWeather(
                    session,
                    weatherEntityController.selectWeatherEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            weatherEntityController.insertWeatherEntity(
                    session,
                    WeatherEntityConverter.convert(location, weather)
            );
            dailyEntityController.deleteDailyEntityList(
                    session,
                    dailyEntityController.selectDailyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            dailyEntityController.insertDailyList(
                    session,
                    DailyEntityConverter.convertToEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getDailyForecast()
                    )
            );
            hourlyEntityController.deleteHourlyEntityList(
                    session,
                    hourlyEntityController.selectHourlyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            hourlyEntityController.insertHourlyList(
                    session,
                    HourlyEntityConverter.convertToEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getHourlyForecast()
                    )
            );
            minutelyEntityController.deleteMinutelyEntityList(
                    session,
                    minutelyEntityController.selectMinutelyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            minutelyEntityController.insertMinutelyList(
                    session,
                    MinutelyEntityConverter.convertToEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getMinutelyForecast()
                    )
            );
            alertEntityController.deleteAlertList(
                    session,
                    alertEntityController.selectLocationAlertEntity(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            alertEntityController.insertAlertList(
                    session,
                    AlertEntityConverter.convertToEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getAlertList()
                    )
            );

            historyEntityController.deleteLocationHistoryEntity(
                    session,
                    historyEntityController.selectHistoryEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            historyEntityController.insertHistoryEntity(
                    session,
                    HistoryEntityConverter.convert(
                            location.getCityId(), location.getWeatherSource(), weather
                    )
            );
            if (weather.getYesterday() != null) {
                historyEntityController.insertHistoryEntity(
                        session,
                        HistoryEntityConverter.convert(
                                location.getCityId(), location.getWeatherSource(), weather.getYesterday()
                        )
                );
            }
            return true;
        });
    }

    @Nullable
    public Weather readWeather(@NonNull Location location) {
        WeatherEntity weatherEntity = weatherEntityController.selectWeatherEntity(
                session,location.getCityId(), location.getWeatherSource());
        if (weatherEntity == null) {
            return null;
        }

        HistoryEntity historyEntity = historyEntityController.selectYesterdayHistoryEntity(
                session,location.getCityId(), location.getWeatherSource(),weatherEntity.updateDate);
        return WeatherEntityConverter.convert(weatherEntity, historyEntity);
    }

    public void deleteWeather(@NonNull Location location) {
        session.callInTxNoException(() -> {
            weatherEntityController.deleteWeather(
                    session,
                    weatherEntityController.selectWeatherEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            historyEntityController.deleteLocationHistoryEntity(
                    session,
                    historyEntityController.selectHistoryEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            dailyEntityController.deleteDailyEntityList(
                    session,
                    dailyEntityController.selectDailyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            hourlyEntityController.deleteHourlyEntityList(
                    session,
                    hourlyEntityController.selectHourlyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            minutelyEntityController.deleteMinutelyEntityList(
                    session,
                    minutelyEntityController.selectMinutelyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            alertEntityController.deleteAlertList(
                    session,
                    alertEntityController.selectLocationAlertEntity(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            return true;
        });

        DbUtils.vacuum(new DaoMaster(openHelper.getWritableDatabase()).getDatabase());
    }

    // history.

    public History readHistory(@NonNull Location location, @NonNull Weather weather) {
        return HistoryEntityConverter.convert(
                historyEntityController.selectYesterdayHistoryEntity(
                        session,
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getBase().getPublishDate()
                )
        );
    }

    // chinese city.

    public void ensureChineseCityList(Context context) {
        if (countChineseCity() < 3216) {
            synchronized (writingLock) {
                if (countChineseCity() < 3216) {
                    List<ChineseCity> list = FileUtils.readCityList(context);

                    chineseCityEntityController.deleteChineseCityEntityList(session);
                    chineseCityEntityController.insertChineseCityEntityList(
                            session, ChineseCityEntityConverter.convertToEntityList(list));
                }
            }
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String name) {
        ChineseCityEntity entity = chineseCityEntityController.selectChineseCityEntity(session, name);
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
                session, province, city, district);
        if (entity != null) {
            return ChineseCityEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(float latitude, float longitude) {
        ChineseCityEntity entity = chineseCityEntityController.selectChineseCityEntity(
                session, latitude, longitude);
        if (entity != null) {
            return ChineseCityEntityConverter.convertToModule(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<ChineseCity> readChineseCityList(@NonNull String name) {
        return ChineseCityEntityConverter.convertToModuleList(
                chineseCityEntityController.selectChineseCityEntityList(session, name));
    }

    public int countChineseCity() {
        return chineseCityEntityController.countChineseCityEntity(session);
    }
}


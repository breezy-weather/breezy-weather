package wangdaye.com.geometricweather.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import wangdaye.com.geometricweather.db.generator.AlertEntityGenerator;
import wangdaye.com.geometricweather.db.generator.ChineseCityEntityGenerator;
import wangdaye.com.geometricweather.db.generator.DailyEntityGenerator;
import wangdaye.com.geometricweather.db.generator.HistoryEntityGenerator;
import wangdaye.com.geometricweather.db.generator.HourlyEntityGenerator;
import wangdaye.com.geometricweather.db.generator.LocationEntityGenerator;
import wangdaye.com.geometricweather.db.generator.MinutelyEntityGenerator;
import wangdaye.com.geometricweather.db.generator.WeatherEntityGenerator;
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

    private final DaoSession session;
    private final Object writingLock;

    private final static String DATABASE_NAME = "Geometric_Weather_db";

    private DatabaseHelper(Context c) {
        session = new DaoMaster(
                new DatabaseOpenHelper(c, DATABASE_NAME, null).getWritableDatabase()
        ).newSession();
        writingLock = new Object();
    }

    // location.

    public void writeLocation(@NonNull Location location) {
        LocationEntity entity = LocationEntityGenerator.generate(location);

        session.callInTxNoException(() -> {
            if (LocationEntityController.selectLocationEntity(session, location.getFormattedId()) == null) {
                LocationEntityController.insertLocationEntity(session, entity);
            } else {
                LocationEntityController.updateLocationEntity(session, entity);
            }
            return true;
        });
    }

    public void writeLocationList(@NonNull List<Location> list) {
        session.callInTxNoException(() -> {
            LocationEntityController.deleteLocationEntityList(session);
            LocationEntityController.insertLocationEntityList(
                    session,
                    LocationEntityGenerator.generateEntityList(list)
            );
            return true;
        });
    }

    public void deleteLocation(@NonNull Location location) {
        LocationEntityController.deleteLocationEntity(
                session, LocationEntityGenerator.generate(location));
    }

    @Nullable
    public Location readLocation(@NonNull Location location) {
        return readLocation(location.getFormattedId());
    }

    @Nullable
    public Location readLocation(@NonNull String formattedId) {
        LocationEntity entity = LocationEntityController.selectLocationEntity(session, formattedId);
        if (entity != null) {
            return LocationEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<Location> readLocationList() {
        List<LocationEntity> entityList = LocationEntityController.selectLocationEntityList(session);

        if (entityList.size() == 0) {
            synchronized (writingLock) {
                if (countLocation() == 0) {
                    LocationEntity entity = LocationEntityGenerator.generate(
                            Location.buildLocal());
                    entityList.add(entity);

                    LocationEntityController.insertLocationEntityList(session, entityList);

                    return LocationEntityGenerator.generateModuleList(entityList);
                }
            }
        }

        return LocationEntityGenerator.generateModuleList(entityList);
    }

    public int countLocation() {
        return LocationEntityController.countLocationEntity(session);
    }

    // weather.

    public void writeWeather(@NonNull Location location, @NonNull Weather weather) {
        session.callInTxNoException(() -> {
            deleteWeather(location);

            WeatherEntityController.insertWeatherEntity(
                    session,
                    WeatherEntityGenerator.generate(location, weather)
            );
            DailyEntityController.insertDailyList(
                    session,
                    DailyEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getDailyForecast()
                    )
            );
            HourlyEntityController.insertHourlyList(
                    session,
                    HourlyEntityGenerator.generateEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getHourlyForecast()
                    )
            );
            MinutelyEntityController.insertMinutelyList(
                    session,
                    MinutelyEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getMinutelyForecast()
                    )
            );
            AlertEntityController.insertAlertList(
                    session,
                    AlertEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getAlertList()
                    )
            );
            HistoryEntityController.insertHistoryEntity(
                    session,
                    HistoryEntityGenerator.generate(
                            location.getCityId(), location.getWeatherSource(), weather
                    )
            );
            if (weather.getYesterday() != null) {
                HistoryEntityController.insertHistoryEntity(
                        session,
                        HistoryEntityGenerator.generate(
                                location.getCityId(), location.getWeatherSource(), weather.getYesterday()
                        )
                );
            }
            return true;
        });
    }

    @Nullable
    public Weather readWeather(@NonNull Location location) {
        WeatherEntity weatherEntity = WeatherEntityController.selectWeatherEntity(
                session,location.getCityId(), location.getWeatherSource());
        if (weatherEntity == null) {
            return null;
        }

        HistoryEntity historyEntity = HistoryEntityController.selectYesterdayHistoryEntity(
                session,location.getCityId(), location.getWeatherSource(),weatherEntity.publishDate);

        return WeatherEntityGenerator.generate(weatherEntity, historyEntity);
    }

    public void deleteWeather(@NonNull Location location) {
        session.callInTxNoException(() -> {
            WeatherEntityController.deleteWeather(
                    session,
                    WeatherEntityController.selectWeatherEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            HistoryEntityController.deleteLocationHistoryEntity(
                    session,
                    HistoryEntityController.selectHistoryEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            DailyEntityController.deleteDailyEntityList(
                    session,
                    DailyEntityController.selectDailyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            HourlyEntityController.deleteHourlyEntityList(
                    session,
                    HourlyEntityController.selectHourlyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            MinutelyEntityController.deleteMinutelyEntityList(
                    session,
                    MinutelyEntityController.selectMinutelyEntityList(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            AlertEntityController.deleteAlertList(
                    session,
                    AlertEntityController.selectLocationAlertEntity(
                            session,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            return true;
        });
    }

    // history.

    public History readHistory(@NonNull Location location, @NonNull Weather weather) {
        return HistoryEntityGenerator.generate(
                HistoryEntityController.selectYesterdayHistoryEntity(
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

                    ChineseCityEntityController.deleteChineseCityEntityList(session);
                    ChineseCityEntityController.insertChineseCityEntityList(
                            session, ChineseCityEntityGenerator.generateEntityList(list));
                }
            }
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String name) {
        ChineseCityEntity entity = ChineseCityEntityController.selectChineseCityEntity(session, name);
        if (entity != null) {
            return ChineseCityEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String province,
                                       @NonNull String city,
                                       @NonNull String district) {
        ChineseCityEntity entity = ChineseCityEntityController.selectChineseCityEntity(
                session, province, city, district);
        if (entity != null) {
            return ChineseCityEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(float latitude, float longitude) {
        ChineseCityEntity entity = ChineseCityEntityController.selectChineseCityEntity(
                session, latitude, longitude);
        if (entity != null) {
            return ChineseCityEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<ChineseCity> readChineseCityList(@NonNull String name) {
        return ChineseCityEntityGenerator.generateModuleList(
                ChineseCityEntityController.selectChineseCityEntityList(session, name));
    }

    public int countChineseCity() {
        return ChineseCityEntityController.countChineseCityEntity(session);
    }
}


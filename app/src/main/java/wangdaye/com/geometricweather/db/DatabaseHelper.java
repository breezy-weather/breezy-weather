package wangdaye.com.geometricweather.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.objectbox.BoxStore;
import wangdaye.com.geometricweather.common.basic.models.ChineseCity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.History;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.FileUtils;
import wangdaye.com.geometricweather.db.controllers.AlertEntityController;
import wangdaye.com.geometricweather.db.controllers.ChineseCityEntityController;
import wangdaye.com.geometricweather.db.controllers.DailyEntityController;
import wangdaye.com.geometricweather.db.controllers.HistoryEntityController;
import wangdaye.com.geometricweather.db.controllers.HourlyEntityController;
import wangdaye.com.geometricweather.db.controllers.LocationEntityController;
import wangdaye.com.geometricweather.db.controllers.MinutelyEntityController;
import wangdaye.com.geometricweather.db.controllers.WeatherEntityController;
import wangdaye.com.geometricweather.db.entities.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entities.HistoryEntity;
import wangdaye.com.geometricweather.db.entities.LocationEntity;
import wangdaye.com.geometricweather.db.entities.MyObjectBox;
import wangdaye.com.geometricweather.db.entities.WeatherEntity;
import wangdaye.com.geometricweather.db.generators.AlertEntityGenerator;
import wangdaye.com.geometricweather.db.generators.ChineseCityEntityGenerator;
import wangdaye.com.geometricweather.db.generators.DailyEntityGenerator;
import wangdaye.com.geometricweather.db.generators.HistoryEntityGenerator;
import wangdaye.com.geometricweather.db.generators.HourlyEntityGenerator;
import wangdaye.com.geometricweather.db.generators.LocationEntityGenerator;
import wangdaye.com.geometricweather.db.generators.MinutelyEntityGenerator;
import wangdaye.com.geometricweather.db.generators.WeatherEntityGenerator;

/**
 * Database helper
 * */

public class DatabaseHelper {

    private static volatile DatabaseHelper sInstance;
    public static DatabaseHelper getInstance(Context c) {
        if (sInstance == null) {
            synchronized (DatabaseHelper.class) {
                sInstance = new DatabaseHelper(c);
            }
        }
        return sInstance;
    }

    private final BoxStore boxStore;
    private final Object mWritingLock;

    private final static String DATABASE_NAME = "Geometric_Weather_db";

    private DatabaseHelper(Context c) {
        boxStore = MyObjectBox.builder()
                .androidContext(c)
                .build();
        mWritingLock = new Object();
    }

    // location.

    public void writeLocation(@NonNull Location location) {
        LocationEntity entity = LocationEntityGenerator.generate(location);

        boxStore.callInTxNoException(() -> {
            if (LocationEntityController.selectLocationEntity(boxStore, location.getFormattedId()) == null) {
                LocationEntityController.insertLocationEntity(boxStore, entity);
            } else {
                LocationEntityController.updateLocationEntity(boxStore, entity);
            }
            return true;
        });
    }

    public void writeLocationList(@NonNull List<Location> list) {
        boxStore.callInTxNoException(() -> {
            LocationEntityController.deleteLocationEntityList(boxStore);
            LocationEntityController.insertLocationEntityList(
                    boxStore,
                    LocationEntityGenerator.generateEntityList(list)
            );
            return true;
        });
    }

    public void deleteLocation(@NonNull Location location) {
        LocationEntityController.deleteLocationEntity(
                boxStore, LocationEntityGenerator.generate(location));
    }

    @Nullable
    public Location readLocation(@NonNull Location location) {
        return readLocation(location.getFormattedId());
    }

    @Nullable
    public Location readLocation(@NonNull String formattedId) {
        LocationEntity entity = LocationEntityController.selectLocationEntity(boxStore, formattedId);
        if (entity != null) {
            return LocationEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<Location> readLocationList() {
        List<LocationEntity> entityList = LocationEntityController.selectLocationEntityList(boxStore);

        if (entityList.size() == 0) {
            synchronized (mWritingLock) {
                if (countLocation() == 0) {
                    LocationEntity entity = LocationEntityGenerator.generate(
                            Location.buildLocal());
                    entityList.add(entity);

                    LocationEntityController.insertLocationEntityList(boxStore, entityList);

                    return LocationEntityGenerator.generateModuleList(entityList);
                }
            }
        }

        return LocationEntityGenerator.generateModuleList(entityList);
    }

    public int countLocation() {
        return LocationEntityController.countLocationEntity(boxStore);
    }

    // weather.

    public void writeWeather(@NonNull Location location, @NonNull Weather weather) {
        boxStore.callInTxNoException(() -> {
            deleteWeather(location);

            WeatherEntityController.insertWeatherEntity(
                    boxStore,
                    WeatherEntityGenerator.generate(location, weather)
            );
            DailyEntityController.insertDailyList(
                    boxStore,
                    DailyEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getDailyForecast()
                    )
            );
            HourlyEntityController.insertHourlyList(
                    boxStore,
                    HourlyEntityGenerator.generateEntityList(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getHourlyForecast()
                    )
            );
            MinutelyEntityController.insertMinutelyList(
                    boxStore,
                    MinutelyEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getMinutelyForecast()
                    )
            );
            AlertEntityController.insertAlertList(
                    boxStore,
                    AlertEntityGenerator.generate(
                            location.getCityId(),
                            location.getWeatherSource(),
                            weather.getAlertList()
                    )
            );
            HistoryEntityController.insertHistoryEntity(
                    boxStore,
                    HistoryEntityGenerator.generate(
                            location.getCityId(), location.getWeatherSource(), weather
                    )
            );
            if (weather.getYesterday() != null) {
                HistoryEntityController.insertHistoryEntity(
                        boxStore,
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
                boxStore,location.getCityId(), location.getWeatherSource());
        if (weatherEntity == null) {
            return null;
        }

        HistoryEntity historyEntity = HistoryEntityController.selectYesterdayHistoryEntity(
                boxStore,location.getCityId(), location.getWeatherSource(), weatherEntity.publishDate, location.getTimeZone());

        return WeatherEntityGenerator.generate(weatherEntity, historyEntity, this.boxStore);
    }

    public void deleteWeather(@NonNull Location location) {
        boxStore.callInTxNoException(() -> {
            WeatherEntityController.deleteWeather(
                    boxStore,
                    WeatherEntityController.selectWeatherEntityList(
                            boxStore,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            HistoryEntityController.deleteLocationHistoryEntity(
                    boxStore,
                    HistoryEntityController.selectHistoryEntityList(
                            boxStore,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            DailyEntityController.deleteDailyEntityList(
                    boxStore,
                    DailyEntityController.selectDailyEntityList(
                            boxStore,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            HourlyEntityController.deleteHourlyEntityList(
                    boxStore,
                    HourlyEntityController.selectHourlyEntityList(
                            boxStore,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            MinutelyEntityController.deleteMinutelyEntityList(
                    boxStore,
                    MinutelyEntityController.selectMinutelyEntityList(
                            boxStore,
                            location.getCityId(),
                            location.getWeatherSource()
                    )
            );
            AlertEntityController.deleteAlertList(
                    boxStore,
                    AlertEntityController.selectLocationAlertEntity(
                            boxStore,
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
                        boxStore,
                        location.getCityId(),
                        location.getWeatherSource(),
                        weather.getBase().getPublishDate(),
                        location.getTimeZone()
                )
        );
    }

    // chinese city.

    public void ensureChineseCityList(Context context) {
        if (countChineseCity() < 3216) {
            synchronized (mWritingLock) {
                if (countChineseCity() < 3216) {
                    List<ChineseCity> list = FileUtils.readCityList(context);

                    ChineseCityEntityController.deleteChineseCityEntityList(boxStore);
                    ChineseCityEntityController.insertChineseCityEntityList(
                            boxStore, ChineseCityEntityGenerator.generateEntityList(list));
                }
            }
        }
    }

    @Nullable
    public ChineseCity readChineseCity(@NonNull String name) {
        ChineseCityEntity entity = ChineseCityEntityController.selectChineseCityEntity(boxStore, name);
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
                boxStore, province, city, district);
        if (entity != null) {
            return ChineseCityEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @Nullable
    public ChineseCity readChineseCity(float latitude, float longitude) {
        ChineseCityEntity entity = ChineseCityEntityController.selectChineseCityEntity(
                boxStore, latitude, longitude);
        if (entity != null) {
            return ChineseCityEntityGenerator.generate(entity);
        } else {
            return null;
        }
    }

    @NonNull
    public List<ChineseCity> readChineseCityList(@NonNull String name) {
        return ChineseCityEntityGenerator.generateModuleList(
                ChineseCityEntityController.selectChineseCityEntityList(boxStore, name));
    }

    public int countChineseCity() {
        return ChineseCityEntityController.countChineseCityEntity(boxStore);
    }
}


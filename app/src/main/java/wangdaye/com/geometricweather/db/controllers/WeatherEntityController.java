package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.WeatherEntity;
import wangdaye.com.geometricweather.db.entities.WeatherEntity_;

public class WeatherEntityController {

    // insert.

    public static void insertWeatherEntity(@NonNull BoxStore boxStore,
                                           @NonNull WeatherEntity entity) {
        boxStore.boxFor(WeatherEntity.class).put(entity);
    }

    // delete.

    public static void deleteWeather(@NonNull BoxStore boxStore,
                                     @NonNull List<WeatherEntity> entityList) {
        boxStore.boxFor(WeatherEntity.class).remove(entityList);
    }

    // select.

    @Nullable
    public static WeatherEntity selectWeatherEntity(@NonNull BoxStore boxStore,
                                                    @NonNull String cityId,
                                                    @NonNull WeatherSource source) {
        List<WeatherEntity> entityList = selectWeatherEntityList(boxStore, cityId, source);
        if (entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    public static List<WeatherEntity> selectWeatherEntityList(@NonNull BoxStore boxStore,
                                                              @NonNull String cityId,
                                                              @NonNull WeatherSource source) {
        Query<WeatherEntity> query = boxStore.boxFor(WeatherEntity.class)
                .query(WeatherEntity_.cityId.equal(cityId)
                        .and(WeatherEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<WeatherEntity> results = query.find();
        query.close();
        return results;
    }
}

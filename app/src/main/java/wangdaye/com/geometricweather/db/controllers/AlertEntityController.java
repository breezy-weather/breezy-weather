package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.AlertEntity;
import wangdaye.com.geometricweather.db.entities.AlertEntity_;

public class AlertEntityController {

    // insert.

    public static void insertAlertList(@NonNull BoxStore boxStore,
                                       @NonNull List<AlertEntity> entityList) {
        boxStore.boxFor(AlertEntity.class).put(entityList);
    }

    // delete.

    public static void deleteAlertList(@NonNull BoxStore boxStore,
                                       @NonNull List<AlertEntity> entityList) {
        boxStore.boxFor(AlertEntity.class).remove(entityList);
    }

    // search.

    public static List<AlertEntity> selectLocationAlertEntity(@NonNull BoxStore boxStore,
                                                              @NonNull String cityId,
                                                              @NonNull WeatherSource source) {
        Query<AlertEntity> query = boxStore.boxFor(AlertEntity.class)
                .query(AlertEntity_.cityId.equal(cityId)
                        .and(AlertEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<AlertEntity> results = query.find();
        query.close();
        return results;
    }
}

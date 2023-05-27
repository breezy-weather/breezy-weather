package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.DailyEntity;
import wangdaye.com.geometricweather.db.entities.DailyEntity_;

public class DailyEntityController {

    // insert.

    public static void insertDailyList(@NonNull BoxStore boxStore,
                                       @NonNull List<DailyEntity> entityList) {
        boxStore.boxFor(DailyEntity.class).put(entityList);
    }

    // delete.

    public static void deleteDailyEntityList(@NonNull BoxStore boxStore,
                                             @NonNull List<DailyEntity> entityList) {
        boxStore.boxFor(DailyEntity.class).remove(entityList);
    }

    // select.

    @NonNull
    public static List<DailyEntity> selectDailyEntityList(@NonNull BoxStore boxStore,
                                                          @NonNull String cityId,
                                                          @NonNull WeatherSource source) {
        Query<DailyEntity> query = boxStore.boxFor(DailyEntity.class)
                .query(DailyEntity_.cityId.equal(cityId)
                        .and(DailyEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<DailyEntity> results = query.find();
        query.close();
        return results;
    }
}

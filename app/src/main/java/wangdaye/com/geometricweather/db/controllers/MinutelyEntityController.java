package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.MinutelyEntity;
import wangdaye.com.geometricweather.db.entities.MinutelyEntity_;

public class MinutelyEntityController {

    // insert.

    public static void insertMinutelyList(@NonNull BoxStore boxStore,
                                          @NonNull List<MinutelyEntity> entityList) {
        boxStore.boxFor(MinutelyEntity.class).put(entityList);
    }

    // delete.

    public static void deleteMinutelyEntityList(@NonNull BoxStore boxStore,
                                                @NonNull List<MinutelyEntity> entityList) {
        boxStore.boxFor(MinutelyEntity.class).remove(entityList);
    }

    // select.

    public static List<MinutelyEntity> selectMinutelyEntityList(@NonNull BoxStore boxStore,
                                                                @NonNull String cityId, @NonNull WeatherSource source) {
        Query<MinutelyEntity> query = boxStore.boxFor(MinutelyEntity.class)
                .query(MinutelyEntity_.cityId.equal(cityId)
                        .and(MinutelyEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<MinutelyEntity> results = query.find();
        query.close();
        return results;
    }
}

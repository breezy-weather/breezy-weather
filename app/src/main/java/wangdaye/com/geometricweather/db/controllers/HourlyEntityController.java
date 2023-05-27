package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.HourlyEntity;
import wangdaye.com.geometricweather.db.entities.HourlyEntity_;

public class HourlyEntityController {

    // insert.

    public static void insertHourlyList(@NonNull BoxStore boxStore,
                                        @NonNull List<HourlyEntity> entityList) {
        boxStore.boxFor(HourlyEntity.class).put(entityList);
    }

    // delete.

    public static void deleteHourlyEntityList(@NonNull BoxStore boxStore,
                                              @NonNull List<HourlyEntity> entityList) {
        boxStore.boxFor(HourlyEntity.class).remove(entityList);
    }

    // select.

    public static List<HourlyEntity> selectHourlyEntityList(@NonNull BoxStore boxStore,
                                                            @NonNull String cityId,
                                                            @NonNull WeatherSource source) {
        Query<HourlyEntity> query = boxStore.boxFor(HourlyEntity.class)
                .query(HourlyEntity_.cityId.equal(cityId)
                        .and(HourlyEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<HourlyEntity> results = query.find();
        query.close();
        return results;
    }
}

package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.db.entities.LocationEntity;
import wangdaye.com.geometricweather.db.entities.LocationEntity_;

public class LocationEntityController {

    // insert.

    public static void insertLocationEntity(@NonNull BoxStore boxStore,
                                            @NonNull LocationEntity entity) {
        boxStore.boxFor(LocationEntity.class).put(entity);
    }

    public static void insertLocationEntityList(@NonNull BoxStore boxStore,
                                                @NonNull List<LocationEntity> entityList) {
        if (entityList.size() != 0) {
            boxStore.boxFor(LocationEntity.class).put(entityList);
        }
    }

    // delete.

    public static void deleteLocationEntity(@NonNull BoxStore boxStore,
                                            @NonNull LocationEntity entity) {
        Query<LocationEntity> query = boxStore.boxFor(LocationEntity.class)
                .query(LocationEntity_.formattedId.equal(entity.formattedId))
                .build();
        List<LocationEntity> results = query.find();
        query.close();
        boxStore.boxFor(LocationEntity.class).remove(results);
    }

    public static void deleteLocationEntityList(@NonNull BoxStore boxStore) {
        boxStore.boxFor(LocationEntity.class).removeAll();
    }

    // update.

    public static void updateLocationEntity(@NonNull BoxStore boxStore,
                                            @NonNull LocationEntity entity) {
        boxStore.boxFor(LocationEntity.class).put(entity);
    }

    // select.

    @Nullable
    public static LocationEntity selectLocationEntity(@NonNull BoxStore boxStore,
                                                      @NonNull String formattedId) {
        Query<LocationEntity> query = boxStore.boxFor(LocationEntity.class)
                .query(LocationEntity_.formattedId.equal(formattedId))
                .build();
        List<LocationEntity> entityList = query.find();
        query.close();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    public static List<LocationEntity> selectLocationEntityList(@NonNull BoxStore boxStore) {
        Query<LocationEntity> query = boxStore.boxFor(LocationEntity.class).query().build();
        List<LocationEntity> results = query.find();
        query.close();
        return results;
    }

    public static int countLocationEntity(@NonNull BoxStore boxStore) {
        Query<LocationEntity> query = boxStore.boxFor(LocationEntity.class).query().build();
        long count = query.count();
        query.close();
        return (int) count;
    }
}

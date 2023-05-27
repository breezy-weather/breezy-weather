package wangdaye.com.geometricweather.db.controllers;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.query.QueryCondition;
import wangdaye.com.geometricweather.db.entities.ChineseCityEntity;
import wangdaye.com.geometricweather.db.entities.ChineseCityEntity_;

public class ChineseCityEntityController {

    // insert.

    public static void insertChineseCityEntityList(@NonNull BoxStore boxStore,
                                                   @NonNull List<ChineseCityEntity> entityList) {
        if (entityList.size() != 0) {
            boxStore.boxFor(ChineseCityEntity.class).put(entityList);
        }
    }

    // delete.

    public static void deleteChineseCityEntityList(@NonNull BoxStore boxStore) {
        boxStore.boxFor(ChineseCityEntity.class).removeAll();
    }

    // select.

    @Nullable
    public static ChineseCityEntity selectChineseCityEntity(@NonNull BoxStore boxStore,
                                                            @NonNull String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        Box<ChineseCityEntity> chineseCityEntityBox = boxStore.boxFor(ChineseCityEntity.class);

        Query<ChineseCityEntity> query = chineseCityEntityBox.query(
                ChineseCityEntity_.district.equal(name)
                        .or(ChineseCityEntity_.city.equal(name))
        ).build();

        List<ChineseCityEntity> entityList = query.find();
        query.close();
        if (entityList == null || entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @Nullable
    public static ChineseCityEntity selectChineseCityEntity(@NonNull BoxStore boxStore,
                                                            @NonNull String province,
                                                            @NonNull String city,
                                                            @NonNull String district) {
        Box<ChineseCityEntity> chineseCityEntityBox = boxStore.boxFor(ChineseCityEntity.class);
        List<QueryCondition<ChineseCityEntity>> conditionList = new ArrayList<>();
        conditionList.add(
                ChineseCityEntity_.district.equal(district)
                        .and(ChineseCityEntity_.city.equal(city))
        );
        conditionList.add(
                ChineseCityEntity_.district.equal(district)
                        .and(ChineseCityEntity_.province.equal(city))
        );
        conditionList.add(
                ChineseCityEntity_.city.equal(district)
                        .and(ChineseCityEntity_.province.equal(city))
        );
        conditionList.add(ChineseCityEntity_.city.equal(city));
        conditionList.add(
                ChineseCityEntity_.district.equal(district)
                        .and(ChineseCityEntity_.province.equal(city))
        );
        conditionList.add(
                ChineseCityEntity_.district.equal(district)
                        .and(ChineseCityEntity_.city.equal(city))
        );
        conditionList.add(ChineseCityEntity_.district.equal(city));
        conditionList.add(ChineseCityEntity_.city.equal(district));

        Query<ChineseCityEntity> query;
        List<ChineseCityEntity> entityList;
        for (QueryCondition<ChineseCityEntity> c : conditionList) {
            try {
                query = chineseCityEntityBox.query(c).build();
                entityList = query.find();
                query.close();
            } catch (Exception e) {
                entityList = null;
            }
            if (entityList != null && entityList.size() > 0) {
                return entityList.get(0);
            }
        }

        return null;
    }

    @Nullable
    public static ChineseCityEntity selectChineseCityEntity(@NonNull BoxStore boxStore,
                                                            float latitude,
                                                            float longitude) {
        Query<ChineseCityEntity> query = boxStore.boxFor(ChineseCityEntity.class).query().build();
        List<ChineseCityEntity> entityList = query.find();
        query.close();

        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < entityList.size(); i++) {
            double distance = Math.pow(latitude - Double.parseDouble(entityList.get(i).latitude), 2)
                    + Math.pow(longitude - Double.parseDouble(entityList.get(i).longitude), 2);
            if (distance < minDistance) {
                minIndex = i;
                minDistance = distance;
            }
        }
        if (0 <= minIndex && minIndex < entityList.size()) {
            return entityList.get(minIndex);
        } else {
            return null;
        }
    }

    @NonNull
    public static List<ChineseCityEntity> selectChineseCityEntityList(@NonNull BoxStore boxStore,
                                                                      @NonNull String name) {
        if (TextUtils.isEmpty(name)) {
            return new ArrayList<>();
        }

        Box<ChineseCityEntity> chineseCityEntityBox = boxStore.boxFor(ChineseCityEntity.class);

        Query<ChineseCityEntity> query = chineseCityEntityBox.query(
                ChineseCityEntity_.district.contains(name)
                        .and(ChineseCityEntity_.city.contains(name))
                        .and(ChineseCityEntity_.province.contains(name))
        ).build();
        List<ChineseCityEntity> results = query.find();
        query.close();
        return results;
    }

    public static int countChineseCityEntity(@NonNull BoxStore boxStore) {
        return (int) boxStore.boxFor(ChineseCityEntity.class).count();
    }
}

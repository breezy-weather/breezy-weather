package wangdaye.com.geometricweather.db.controllers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.HistoryEntity;
import wangdaye.com.geometricweather.db.entities.HistoryEntity_;

public class HistoryEntityController {

    // insert.

    public static void insertHistoryEntity(@NonNull BoxStore boxStore, @NonNull HistoryEntity entity) {
        boxStore.boxFor(HistoryEntity.class).put(entity);
    }

    // delete.

    public static void deleteLocationHistoryEntity(@NonNull BoxStore boxStore,
                                                   @NonNull List<HistoryEntity> entityList) {
        boxStore.boxFor(HistoryEntity.class).remove(entityList);
    }

    // select.

    @SuppressLint("SimpleDateFormat")
    @Nullable
    public static HistoryEntity selectYesterdayHistoryEntity(@NonNull BoxStore boxStore,
                                                             @NonNull String cityId,
                                                             @NonNull WeatherSource source,
                                                             @NonNull Date currentDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date today = format.parse(format.format(currentDate));
            if (today == null) {
                throw new NullPointerException("Get null Date object.");
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -1);
            Date yesterday = calendar.getTime();

            Query<HistoryEntity> query = boxStore.boxFor(HistoryEntity.class)
                    .query(HistoryEntity_.date.greaterOrEqual(yesterday)
                            .and(HistoryEntity_.date.less(today))
                            .and(HistoryEntity_.cityId.equal(cityId))
                            .and(HistoryEntity_.weatherSource.equal(
                                    new WeatherSourceConverter().convertToDatabaseValue(source)
                            ))
                    ).build();
            List<HistoryEntity> entityList = query.find();
            query.close();

            if (entityList == null || entityList.size() == 0) {
                return null;
            } else {
                return entityList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Nullable
    private static HistoryEntity selectTodayHistoryEntity(@NonNull BoxStore boxStore,
                                                          @NonNull String cityId,
                                                          @NonNull WeatherSource source,
                                                          @NonNull Date currentDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date today = format.parse(format.format(currentDate));
            if (today == null) {
                throw new NullPointerException("Get null Date object.");
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, +1);
            Date tomorrow = calendar.getTime();

            Query<HistoryEntity> query = boxStore.boxFor(HistoryEntity.class)
                    .query(HistoryEntity_.date.greaterOrEqual(today)
                            .and(HistoryEntity_.date.less(tomorrow))
                            .and(HistoryEntity_.cityId.equal(cityId))
                            .and(HistoryEntity_.weatherSource.equal(
                                    new WeatherSourceConverter().convertToDatabaseValue(source)
                            ))
                    ).build();
            List<HistoryEntity> entityList = query.find();
            query.close();
            if (entityList == null || entityList.size() == 0) {
                return null;
            } else {
                return entityList.get(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static List<HistoryEntity> selectHistoryEntityList(@NonNull BoxStore boxStore,
                                                              @NonNull String cityId,
                                                              @NonNull WeatherSource source) {
        Query<HistoryEntity> query = boxStore.boxFor(HistoryEntity.class)
                .query(HistoryEntity_.cityId.equal(cityId)
                        .and(HistoryEntity_.weatherSource.equal(
                                new WeatherSourceConverter().convertToDatabaseValue(source)
                        ))
                ).build();
        List<HistoryEntity> results = query.find();
        query.close();
        return results;
    }
}

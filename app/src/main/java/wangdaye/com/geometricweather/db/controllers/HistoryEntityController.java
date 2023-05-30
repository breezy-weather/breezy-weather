package wangdaye.com.geometricweather.db.controllers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
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
    @Nullable
    public static HistoryEntity selectYesterdayHistoryEntity(@NonNull BoxStore boxStore,
                                                             @NonNull String cityId,
                                                             @NonNull WeatherSource source,
                                                             @NonNull Date currentDate,
                                                             @NonNull TimeZone timeZone) {
        try {
            Calendar calendar = DisplayUtils.toCalendarWithTimeZone(currentDate, timeZone);
            Date today = calendar.getTime();
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

    @Nullable
    private static HistoryEntity selectTodayHistoryEntity(@NonNull BoxStore boxStore,
                                                          @NonNull String cityId,
                                                          @NonNull WeatherSource source,
                                                          @NonNull Date currentDate,
                                                          @NonNull TimeZone timeZone) {
        try {
            Calendar calendar = DisplayUtils.toCalendarWithTimeZone(currentDate, timeZone);
            Date today = calendar.getTime();
            calendar.add(Calendar.DATE, 1);
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
        } catch (Exception e) {
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

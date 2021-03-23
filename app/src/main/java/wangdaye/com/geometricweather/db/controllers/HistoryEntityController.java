package wangdaye.com.geometricweather.db.controllers;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entities.DaoSession;
import wangdaye.com.geometricweather.db.entities.HistoryEntity;
import wangdaye.com.geometricweather.db.entities.HistoryEntityDao;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;

public class HistoryEntityController extends AbsEntityController {

    // insert.

    public static void insertHistoryEntity(@NonNull DaoSession session, @NonNull HistoryEntity entity) {
        session.getHistoryEntityDao().insert(entity);
    }

    // delete.

    public static void deleteLocationHistoryEntity(@NonNull DaoSession session,
                                                   @NonNull List<HistoryEntity> entityList) {
        session.getHistoryEntityDao().deleteInTx(entityList);
    }

    // select.

    @SuppressLint("SimpleDateFormat")
    @Nullable
    public static HistoryEntity selectYesterdayHistoryEntity(@NonNull DaoSession session,
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

            List<HistoryEntity> entityList = session.getHistoryEntityDao()
                    .queryBuilder()
                    .where(
                            HistoryEntityDao.Properties.Date.ge(yesterday),
                            HistoryEntityDao.Properties.Date.lt(today),
                            HistoryEntityDao.Properties.CityId.eq(cityId),
                            HistoryEntityDao.Properties.WeatherSource.eq(
                                    new WeatherSourceConverter().convertToDatabaseValue(source)
                            )
                    ).list();

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
    private static HistoryEntity selectTodayHistoryEntity(@NonNull DaoSession session,
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

            List<HistoryEntity> entityList = session.getHistoryEntityDao()
                    .queryBuilder()
                    .where(
                            HistoryEntityDao.Properties.Date.ge(today),
                            HistoryEntityDao.Properties.Date.lt(tomorrow),
                            HistoryEntityDao.Properties.CityId.eq(cityId),
                            HistoryEntityDao.Properties.WeatherSource.eq(
                                    new WeatherSourceConverter().convertToDatabaseValue(source)
                            )
                    ).list();
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
    public static List<HistoryEntity> selectHistoryEntityList(@NonNull DaoSession session,
                                                              @NonNull String cityId,
                                                              @NonNull WeatherSource source) {
        return getNonNullList(
                session.getHistoryEntityDao()
                        .queryBuilder()
                        .where(
                                HistoryEntityDao.Properties.CityId.eq(cityId),
                                HistoryEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}

package wangdaye.com.geometricweather.db.controller;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;
import wangdaye.com.geometricweather.db.entity.HistoryEntityDao;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class HistoryEntityController extends AbsEntityController<HistoryEntity> {
    
    public HistoryEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertTodayHistoryEntity(@NonNull String cityId, @NonNull WeatherSource source,
                                         @NonNull Date currentDate, @NonNull HistoryEntity entity) {
        HistoryEntity yesterday = selectYesterdayHistoryEntity(cityId, source, currentDate);
        deleteLocationHistoryEntity(cityId, source);

        HistoryEntityDao dao = getSession().getHistoryEntityDao();
        if (yesterday != null) {
            yesterday.id = null;
            dao.insert(yesterday);
        }
        dao.insert(entity);

        getSession().clear();
    }

    public void insertYesterdayHistoryEntity(@NonNull String cityId, @NonNull WeatherSource source,
                                             @NonNull Date currentDate, @NonNull HistoryEntity entity) {
        HistoryEntity today = selectTodayHistoryEntity(cityId, source, currentDate);
        deleteLocationHistoryEntity(cityId, source);

        HistoryEntityDao dao = getSession().getHistoryEntityDao();
        dao.insert(entity);
        if (today != null) {
            today.id = null;
            dao.insert(today);
        }

        getSession().clear();
    }

    // delete.

    public void deleteLocationHistoryEntity(@NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getHistoryEntityDao().deleteInTx(selectHistoryEntityList(cityId, source));
        getSession().clear();
    }

    // select.

    @SuppressLint("SimpleDateFormat")
    @Nullable
    public HistoryEntity selectYesterdayHistoryEntity(@NonNull String cityId, @NonNull WeatherSource source,
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

            List<HistoryEntity> entityList = getSession().getHistoryEntityDao()
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
    private HistoryEntity selectTodayHistoryEntity(@NonNull String cityId, @NonNull WeatherSource source,
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

            List<HistoryEntity> entityList = getSession().getHistoryEntityDao()
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
    private List<HistoryEntity> selectHistoryEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getHistoryEntityDao()
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

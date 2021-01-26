package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DailyEntity;
import wangdaye.com.geometricweather.db.entity.DailyEntityDao;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.converter.WeatherSourceConverter;

public class DailyEntityController extends AbsEntityController {

    // insert.

    public static void insertDailyList(@NonNull DaoSession session,
                                       @NonNull List<DailyEntity> entityList) {
        session.getDailyEntityDao().insertInTx(entityList);
    }

    // delete.

    public static void deleteDailyEntityList(@NonNull DaoSession session,
                                             @NonNull List<DailyEntity> entityList) {
        session.getDailyEntityDao().deleteInTx(entityList);
    }

    // select.

    @NonNull
    public static List<DailyEntity> selectDailyEntityList(@NonNull DaoSession session,
                                                          @NonNull String cityId,
                                                          @NonNull WeatherSource source) {
        return getNonNullList(
                session.getDailyEntityDao()
                        .queryBuilder()
                        .where(
                                DailyEntityDao.Properties.CityId.eq(cityId),
                                DailyEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}

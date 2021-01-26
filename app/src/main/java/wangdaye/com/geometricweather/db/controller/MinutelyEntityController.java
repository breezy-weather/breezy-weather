package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.MinutelyEntity;
import wangdaye.com.geometricweather.db.entity.MinutelyEntityDao;
import wangdaye.com.geometricweather.db.converter.WeatherSourceConverter;

public class MinutelyEntityController extends AbsEntityController {

    // insert.

    public static void insertMinutelyList(@NonNull DaoSession session,
                                          @NonNull List<MinutelyEntity> entityList) {
        session.getMinutelyEntityDao().insertInTx(entityList);
    }

    // delete.

    public static void deleteMinutelyEntityList(@NonNull DaoSession session,
                                                @NonNull List<MinutelyEntity> entityList) {
        session.getMinutelyEntityDao().deleteInTx(entityList);
    }

    // select.

    public static List<MinutelyEntity> selectMinutelyEntityList(@NonNull DaoSession session,
                                                                @NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                session.getMinutelyEntityDao()
                        .queryBuilder()
                        .where(
                                MinutelyEntityDao.Properties.CityId.eq(cityId),
                                MinutelyEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}

package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntityDao;
import wangdaye.com.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class WeatherEntityController extends AbsEntityController<WeatherEntity> {

    // insert.

    public void insertWeatherEntity(@NonNull DaoSession session,
                                    @NonNull WeatherEntity entity) {
        session.getWeatherEntityDao().insert(entity);
    }

    // delete.

    public void deleteWeather(@NonNull DaoSession session,
                              @NonNull List<WeatherEntity> entityList) {
        session.getWeatherEntityDao().deleteInTx(entityList);
    }

    // select.

    @Nullable
    public WeatherEntity selectWeatherEntity(@NonNull DaoSession session,
                                             @NonNull String cityId, @NonNull WeatherSource source) {
        List<WeatherEntity> entityList = selectWeatherEntityList(session, cityId, source);
        if (entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    public List<WeatherEntity> selectWeatherEntityList(@NonNull DaoSession session,
                                                        @NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                session.getWeatherEntityDao().queryBuilder()
                        .where(
                                WeatherEntityDao.Properties.CityId.eq(cityId),
                                WeatherEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}

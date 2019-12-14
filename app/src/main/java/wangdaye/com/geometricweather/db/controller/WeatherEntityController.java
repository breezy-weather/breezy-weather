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
    
    public WeatherEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertWeatherEntity(@NonNull String cityId, @NonNull WeatherSource source,
                                    @NonNull WeatherEntity entity) {
        deleteWeather(cityId, source);
        getSession().getWeatherEntityDao().insert(entity);
        getSession().clear();
    }

    // delete.

    public void deleteWeather(@NonNull String cityId, @NonNull WeatherSource source) {
        getSession().getWeatherEntityDao().deleteInTx(selectWeatherEntityList(cityId, source));
        getSession().clear();
    }

    // select.

    @Nullable
    public WeatherEntity selectWeatherEntity(@NonNull String cityId, @NonNull WeatherSource source) {
        List<WeatherEntity> entityList = selectWeatherEntityList(cityId, source);
        if (entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    private List<WeatherEntity> selectWeatherEntityList(@NonNull String cityId, @NonNull WeatherSource source) {
        return getNonNullList(
                getSession().getWeatherEntityDao().queryBuilder()
                        .where(
                                WeatherEntityDao.Properties.CityId.eq(cityId),
                                WeatherEntityDao.Properties.WeatherSource.eq(
                                        new WeatherSourceConverter().convertToDatabaseValue(source)
                                )
                        ).list()
        );
    }
}

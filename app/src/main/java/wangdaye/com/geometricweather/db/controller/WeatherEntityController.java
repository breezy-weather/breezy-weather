package wangdaye.com.geometricweather.db.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import wangdaye.com.geometricweather.db.entity.DaoSession;
import wangdaye.com.geometricweather.db.entity.WeatherEntity;
import wangdaye.com.geometricweather.db.entity.WeatherEntityDao;

public class WeatherEntityController extends AbsEntityController<WeatherEntity> {
    
    public WeatherEntityController(DaoSession session) {
        super(session);
    }

    // insert.

    public void insertWeatherEntity(@NonNull String cityId, @NonNull WeatherEntity entity) {
        deleteWeather(cityId);
        getSession().getWeatherEntityDao().insert(entity);
        getSession().clear();
    }

    // delete.

    public void deleteWeather(@NonNull String cityId) {
        getSession().getWeatherEntityDao().deleteInTx(selectWeatherEntityList(cityId));
        getSession().clear();
    }

    // select.

    @Nullable
    public WeatherEntity selectWeatherEntity(@NonNull String cityId) {
        List<WeatherEntity> entityList = selectWeatherEntityList(cityId);
        if (entityList.size() <= 0) {
            return null;
        } else {
            return entityList.get(0);
        }
    }

    @NonNull
    private List<WeatherEntity> selectWeatherEntityList(@NonNull String cityId) {
        return getNonNullList(
                getSession().getWeatherEntityDao().queryBuilder()
                        .where(WeatherEntityDao.Properties.CityId.eq(cityId))
                        .list()
        );
    }
}

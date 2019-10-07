package wangdaye.com.geometricweather.db.converter;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.basic.model.weather.History;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.HistoryEntity;

public class HistoryEntityConverter {

    public static HistoryEntity convert(String cityId, History history) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.date = history.getDate();
        entity.time = history.getTime();
        entity.daytimeTemperature = history.getDaytimeTemperature();
        entity.nighttimeTemperature = history.getNighttimeTemperature();
        return entity;
    }

    public static HistoryEntity convert(String cityId, Weather weather) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.date = weather.getBase().getPublishDate();
        entity.time = weather.getBase().getPublishTime();
        entity.daytimeTemperature = weather.getDailyForecast().get(0).day().getTemperature().getTemperature();
        entity.nighttimeTemperature = weather.getDailyForecast().get(0).night().getTemperature().getTemperature();
        return entity;
    }

    public static History convert(@Nullable HistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new History(
                entity.date,
                entity.time,
                entity.daytimeTemperature,
                entity.nighttimeTemperature
        );
    }
}

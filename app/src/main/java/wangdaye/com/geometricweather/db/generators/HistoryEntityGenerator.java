package wangdaye.com.geometricweather.db.generators;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.weather.History;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.db.converters.WeatherSourceConverter;
import wangdaye.com.geometricweather.db.entities.HistoryEntity;

public class HistoryEntityGenerator {

    public static HistoryEntity generate(String cityId, WeatherSource source, History history) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);
        entity.date = history.getDate();
        entity.time = history.getTime();
        entity.daytimeTemperature = history.getDaytimeTemperature();
        entity.nighttimeTemperature = history.getNighttimeTemperature();
        return entity;
    }

    public static HistoryEntity generate(String cityId, WeatherSource source, Weather weather) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);
        entity.date = weather.getBase().getPublishDate();
        entity.time = weather.getBase().getPublishTime();
        entity.daytimeTemperature = weather.getDailyForecast().get(0).day().getTemperature().getTemperature();
        entity.nighttimeTemperature = weather.getDailyForecast().get(0).night().getTemperature().getTemperature();
        return entity;
    }

    public static History generate(@Nullable HistoryEntity entity) {
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

package com.mbestavros.geometricweather.db.converter;

import androidx.annotation.Nullable;

import com.mbestavros.geometricweather.basic.model.option.provider.WeatherSource;
import com.mbestavros.geometricweather.basic.model.weather.History;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.db.entity.HistoryEntity;
import com.mbestavros.geometricweather.db.propertyConverter.WeatherSourceConverter;

public class HistoryEntityConverter {

    public static HistoryEntity convert(String cityId, WeatherSource source, History history) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);
        entity.date = history.getDate();
        entity.time = history.getTime();
        entity.daytimeTemperature = history.getDaytimeTemperature();
        entity.nighttimeTemperature = history.getNighttimeTemperature();
        return entity;
    }

    public static HistoryEntity convert(String cityId, WeatherSource source, Weather weather) {
        HistoryEntity entity = new HistoryEntity();
        entity.cityId = cityId;
        entity.weatherSource = new WeatherSourceConverter().convertToDatabaseValue(source);
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

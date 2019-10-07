package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.weather.Minutely;
import wangdaye.com.geometricweather.db.entity.MinutelyEntity;

public class MinutelyEntityConverter {

    public static MinutelyEntity convertToEntity(String cityId, Minutely minutely) {
        MinutelyEntity entity = new MinutelyEntity();

        entity.cityId = cityId;
        
        entity.date = minutely.getDate();
        entity.time = minutely.getTime();
        entity.daylight = minutely.isDaylight();

        entity.weatherCode = minutely.getWeatherCode();
        entity.weatherText = minutely.getWeatherText();

        entity.minuteInterval = minutely.getMinuteInterval();
        entity.dbz = minutely.getDbz();
        entity.cloudCover = minutely.getCloudCover();

        return entity;
    }

    public static List<MinutelyEntity> convertToEntityList(String cityId, List<Minutely> minutelyList) {
        List<MinutelyEntity> entityList = new ArrayList<>(minutelyList.size());
        for (Minutely minutely : minutelyList) {
            entityList.add(convertToEntity(cityId, minutely));
        }
        return entityList;
    }

    public static Minutely convertToModule(MinutelyEntity entity) {
        return new Minutely(
                entity.date, entity.time, entity.daylight,
                entity.weatherText, entity.weatherCode,
                entity.minuteInterval, entity.dbz, entity.cloudCover
        );
    }

    public static List<Minutely> convertToModuleList(List<MinutelyEntity> entityList) {
        List<Minutely> dailyList = new ArrayList<>(entityList.size());
        for (MinutelyEntity entity : entityList) {
            dailyList.add(convertToModule(entity));
        }
        return dailyList;
    }
}

package wangdaye.com.geometricweather.db.generators;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.db.entities.LocationEntity;

public class LocationEntityGenerator {

    public static LocationEntity generate(Location location) {
        LocationEntity entity = new LocationEntity();
        entity.formattedId = location.getFormattedId();
        entity.cityId = location.getCityId();
        entity.latitude = location.getLatitude();
        entity.longitude = location.getLongitude();
        entity.timeZone = location.getTimeZone();
        entity.country = location.getCountry();
        entity.province = location.getProvince();
        entity.city = location.getCity();
        entity.district = location.getDistrict();
        entity.weatherSource = location.getWeatherSource();
        entity.currentPosition = location.isCurrentPosition();
        entity.residentPosition = location.isResidentPosition();
        entity.china = location.isChina();
        return entity;
    }

    public static List<LocationEntity> generateEntityList(List<Location> locationList) {
        List<LocationEntity> entityList = new ArrayList<>(locationList.size());
        for (int i = 0; i < locationList.size(); i ++) {
            entityList.add(generate(locationList.get(i)));
        }
        return entityList;
    }

    public static Location generate(LocationEntity entity) {
        return new Location(
                GeneratorUtils.nonNull(entity.cityId),
                entity.latitude,
                entity.longitude,
                entity.timeZone,
                GeneratorUtils.nonNull(entity.country),
                GeneratorUtils.nonNull(entity.province),
                GeneratorUtils.nonNull(entity.city),
                GeneratorUtils.nonNull(entity.district),
                null,
                entity.weatherSource,
                entity.currentPosition,
                entity.residentPosition,
                entity.china
        );
    }

    public static List<Location> generateModuleList(List<LocationEntity> entityList) {
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (LocationEntity entity : entityList) {
            locationList.add(generate(entity));
        }
        return locationList;
    }
}

package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.entity.LocationEntity;

public class LocationEntityConverter {

    public static LocationEntity convertToEntity(Location location) {
        LocationEntity entity = new LocationEntity();
        entity.cityId = location.getCityId();
        entity.latitude = location.getLatitude();
        entity.longitude = location.getLongitude();
        entity.GMTOffset = location.getGMTOffset();
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

    public static List<LocationEntity> convertToEntityList(List<Location> locationList) {
        List<LocationEntity> entityList = new ArrayList<>(locationList.size());
        for (Location location : locationList) {
            entityList.add(convertToEntity(location));
        }
        return entityList;
    }

    public static Location convertToModule(LocationEntity entity) {
        return new Location(
                entity.cityId,
                entity.latitude, entity.longitude, entity.GMTOffset,
                entity.country, entity.province, entity.city, entity.district,
                null, entity.weatherSource,
                entity.currentPosition, entity.residentPosition, entity.china
        );
    }

    public static List<Location> convertToModuleList(List<LocationEntity> entityList) {
        List<Location> locationList = new ArrayList<>(entityList.size());
        for (LocationEntity entity : entityList) {
            locationList.add(convertToModule(entity));
        }
        return locationList;
    }
}

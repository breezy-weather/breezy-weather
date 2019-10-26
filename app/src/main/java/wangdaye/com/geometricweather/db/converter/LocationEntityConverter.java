package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.entity.LocationEntity;

public class LocationEntityConverter {

    public static LocationEntity convertToEntity(Location location, long sequence) {
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
        entity.sequence = sequence;
        return entity;
    }

    public static List<LocationEntity> convertToEntityList(List<Location> locationList) {
        List<LocationEntity> entityList = new ArrayList<>(locationList.size());
        for (int i = 0; i < locationList.size(); i ++) {
            entityList.add(convertToEntity(locationList.get(i), i));
        }
        return entityList;
    }

    public static Location convertToModule(LocationEntity entity) {
        return new Location(
                entity.cityId,
                entity.latitude, entity.longitude, entity.timeZone,
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

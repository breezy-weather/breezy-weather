package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.ChineseCity;
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;

public class ChineseCityEntityConverter {

    public static ChineseCityEntity convertToEntity(ChineseCity city) {
        ChineseCityEntity entity = new ChineseCityEntity();
        entity.cityId = city.getCityId();
        entity.province = city.getProvince();
        entity.city = city.getCity();
        entity.district = city.getDistrict();
        entity.latitude = city.getLatitude();
        entity.longitude = city.getLongitude();
        return entity;
    }

    public static List<ChineseCityEntity> convertToEntityList(List<ChineseCity> cityList) {
        List<ChineseCityEntity> entityList = new ArrayList<>(cityList.size());
        for (ChineseCity city : cityList) {
            entityList.add(convertToEntity(city));
        }
        return entityList;
    }

    public static ChineseCity convertToModule(ChineseCityEntity entity) {
        return new ChineseCity(
                entity.cityId,
                entity.province, entity.city, entity.district,
                entity.latitude, entity.longitude
        );
    }

    public static List<ChineseCity> convertToModuleList(List<ChineseCityEntity> entityList) {
        List<ChineseCity> cityList = new ArrayList<>(entityList.size());
        for (ChineseCityEntity entity : entityList) {
            cityList.add(convertToModule(entity));
        }
        return cityList;
    }
}

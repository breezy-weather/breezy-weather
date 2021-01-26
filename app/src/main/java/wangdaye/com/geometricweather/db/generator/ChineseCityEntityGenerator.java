package wangdaye.com.geometricweather.db.generator;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.ChineseCity;
import wangdaye.com.geometricweather.db.entity.ChineseCityEntity;

public class ChineseCityEntityGenerator {

    public static ChineseCityEntity generate(ChineseCity city) {
        ChineseCityEntity entity = new ChineseCityEntity();
        entity.cityId = city.getCityId();
        entity.province = city.getProvince();
        entity.city = city.getCity();
        entity.district = city.getDistrict();
        entity.latitude = city.getLatitude();
        entity.longitude = city.getLongitude();
        return entity;
    }

    public static List<ChineseCityEntity> generateEntityList(List<ChineseCity> cityList) {
        List<ChineseCityEntity> entityList = new ArrayList<>(cityList.size());
        for (ChineseCity city : cityList) {
            entityList.add(generate(city));
        }
        return entityList;
    }

    public static ChineseCity generate(ChineseCityEntity entity) {
        return new ChineseCity(
                entity.cityId,
                entity.province, entity.city, entity.district,
                entity.latitude, entity.longitude
        );
    }

    public static List<ChineseCity> generateModuleList(List<ChineseCityEntity> entityList) {
        List<ChineseCity> cityList = new ArrayList<>(entityList.size());
        for (ChineseCityEntity entity : entityList) {
            cityList.add(generate(entity));
        }
        return cityList;
    }
}

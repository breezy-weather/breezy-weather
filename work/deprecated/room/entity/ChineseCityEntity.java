package wangdaye.com.geometricweather.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import wangdaye.com.geometricweather.basic.models.ChineseCity;

/**
 * Chinese city entity.
 *
 * {@link ChineseCity}.
 * */

@Entity(tableName = "CHINESE_CITY_ENTITY")
public class ChineseCityEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") public Long id;

    @ColumnInfo(name = "CITY_ID") public String cityId;
    @ColumnInfo(name = "PROVINCE") public String province;
    @ColumnInfo(name = "CITY") public String city;
    @ColumnInfo(name = "DISTRICT") public String district;
    @ColumnInfo(name = "LATITUDE") public String latitude;
    @ColumnInfo(name = "LONGITUDE") public String longitude;
}

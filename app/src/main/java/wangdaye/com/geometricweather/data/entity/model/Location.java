package wangdaye.com.geometricweather.data.entity.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuLocationResult;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Location.
 * */

public class Location
        implements Parcelable {

    public String cityId;
    public String district;
    public String city;
    public String province;
    public String country;
    public String lat;
    public String lon;
    public String source;

    public Weather weather;
    public History history;

    public boolean local;
    public boolean china;

    private static final String NULL_ID = "NULL_ID";

    public Location() {
        this.cityId = NULL_ID;
        this.district = "";
        this.city = "";
        this.province = "";
        this.country = "";
        this.lat = "";
        this.lon = "";
        this.source = "accu";
        this.weather = null;
        this.history = null;
        this.local = false;
        this.china = false;
    }

    public static Location buildLocal() {
        Location location = new Location();
        location.local = true;
        return location;
    }

    public static Location buildDefaultLocation() {
        Location location = new Location();
        location.cityId = "101924";
        location.district = "";
        location.city = "北京";
        location.province = "直辖市";
        location.country = "中国";
        location.lat = "39.904000";
        location.lon = "116.391000";
        location.source = "accu";
        location.local = true;
        location.china = true;
        return location;
    }

    public static Location buildLocation(LocationEntity entity) {
        Location location = new Location();
        location.cityId = entity.cityId;
        location.district = entity.district;
        location.city = entity.city;
        location.province = entity.province;
        location.country = entity.country;
        location.lat = entity.lat;
        location.lon = entity.lon;
        location.source = entity.source;
        location.local = entity.local;
        location.china = entity.china;
        return location;
    }

    public static List<Location> buildLocationListByAccuResult(List<AccuLocationResult> resultList) {
        List<Location> locationList = new ArrayList<>(resultList.size());
        for (int i = 0; i < resultList.size(); i ++) {
            Location location = new Location();
            location.cityId = resultList.get(i).Key;
            location.district = "";
            location.city = resultList.get(i).LocalizedName;
            location.province = resultList.get(i).AdministrativeArea.LocalizedName;
            location.country = resultList.get(i).Country.LocalizedName;
            location.lat = String.valueOf(resultList.get(i).GeoPosition.Latitude);
            location.lon = String.valueOf(resultList.get(i).GeoPosition.Longitude);
            location.source = "accu";
            location.local = false;
            location.china = !TextUtils.isEmpty(resultList.get(i).Country.ID)
                    && (resultList.get(i).Country.ID.equals("CN")
                    || resultList.get(i).Country.ID.equals("cn")
                    || resultList.get(i).Country.ID.equals("HK")
                    || resultList.get(i).Country.ID.equals("hk")
                    || resultList.get(i).Country.ID.equals("TW")
                    || resultList.get(i).Country.ID.equals("tw"));
            locationList.add(location);
        }
        return locationList;
    }

    public static List<Location> buildLocationListByCNWeather(List<CNCityList.CNCity> cityList) {
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < cityList.size(); i ++) {
            Location location = new Location();
            location.cityId = cityList.get(i).requestKey;
            if (cityList.get(i).district.equals("无")) {
                location.district = "";
            } else {
                location.district = cityList.get(i).district;
            }
            location.city = cityList.get(i).city;
            location.province = cityList.get(i).province;
            location.country = "中国";
            location.lat = cityList.get(i).lat;
            location.lon = cityList.get(i).lon;
            location.source = GeometricWeather.getInstance().getChineseSource();
            if (location.source.equals("accu")) {
                location.source = "cn";
            }
            location.local = false;
            location.china = true;
            locationList.add(location);
        }
        return locationList;
    }

    public static List<Location> buildLocationList(AccuLocationResult result) {
        List<Location> locationList = new ArrayList<>();
        Location location = new Location();
        location.cityId = result.Key;
        location.district = "";
        location.city = result.LocalizedName;
        location.province = result.AdministrativeArea.LocalizedName;
        location.country = result.Country.LocalizedName;
        location.lat = String.valueOf(result.GeoPosition.Latitude);
        location.lon = String.valueOf(result.GeoPosition.Longitude);
        location.source = "accu";
        location.local = false;
        location.china = !TextUtils.isEmpty(result.Country.ID)
                && (result.Country.ID.equals("CN")
                || result.Country.ID.equals("cn")
                || result.Country.ID.equals("HK")
                || result.Country.ID.equals("hk")
                || result.Country.ID.equals("TW")
                || result.Country.ID.equals("tw"));
        locationList.add(location);
        return locationList;
    }

    public static List<Location> buildLocationList(CNCityList.CNCity city) {
        List<Location> locationList = new ArrayList<>();
        Location location = new Location();
        location.cityId = city.requestKey;
        if (city.district.equals("无")) {
            location.district = "";
        } else {
            location.district = city.district;
        }
        location.city = city.city;
        location.province = city.province;
        location.country = "中国";
        location.lat = city.lat;
        location.lon = city.lon;
        location.source = GeometricWeather.getInstance().getChineseSource();
        if (location.source.equals("accu")) {
            location.source = "cn";
        }
        location.local = false;
        location.china = true;
        locationList.add(location);
        return locationList;
    }

    public boolean equals(Location location) {
        if (location.isLocal()) {
            return isLocal();
        } else {
            return cityId.equals(location.cityId);
        }
    }

    public Location setLocal() {
        local = true;
        return this;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isUsable() {
        return !cityId.equals(NULL_ID);
    }

    public boolean canUseChineseSource() {
        return LanguageUtils.isChinese(city) && china;
    }

    public String getCityId() {
        return cityId;
    }

    public String getCityName(Context context) {
        if (!TextUtils.isEmpty(district) && !district.equals("市辖区") && !district.equals("无")) {
            return district;
        } else if (!TextUtils.isEmpty(city) && !city.equals("市辖区")) {
            return city;
        } else if (!TextUtils.isEmpty(province)) {
            return province;
        } else if (local) {
            return context.getString(R.string.local);
        } else {
            return "";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cityId);
        dest.writeString(this.district);
        dest.writeString(this.city);
        dest.writeString(this.province);
        dest.writeString(this.country);
        dest.writeString(this.lat);
        dest.writeString(this.lon);
        dest.writeString(this.source);
        dest.writeByte(this.local ? (byte) 1 : (byte) 0);
        dest.writeByte(this.china ? (byte) 1 : (byte) 0);
    }

    protected Location(Parcel in) {
        this.cityId = in.readString();
        this.district = in.readString();
        this.city = in.readString();
        this.province = in.readString();
        this.country = in.readString();
        this.lat = in.readString();
        this.lon = in.readString();
        this.source = in.readString();
        this.local = in.readByte() != 0;
        this.china = in.readByte() != 0;
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
}

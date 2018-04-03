package wangdaye.com.geometricweather.data.entity.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.data.entity.result.accu.AccuLocationResult;
import wangdaye.com.geometricweather.data.entity.table.LocationEntity;

/**
 * Location.
 * */

public class Location
        implements Parcelable {

    public String cityId;
    public String city;
    public String cnty;
    public String lat;
    public String lon;
    public String prov;

    public Weather weather;
    public History history;

    public boolean local;
    private static final String NULL_ID = "NULL_ID";

    public Location() {
        this.cityId = NULL_ID;
        this.city = "";
        this.cnty = "";
        this.lat = "";
        this.lon = "";
        this.prov = "";
        this.weather = null;
        this.history = null;
        this.local = false;
    }

    public static Location buildLocal() {
        Location location = new Location();
        location.local = true;
        return location;
    }

    public static Location buildDefaultLocation() {
        Location location = new Location();
        location.cityId = "101924";
        location.city = "北京";
        location.cnty = "中国";
        location.lat = "39.904000";
        location.lon = "116.391000";
        location.prov = "直辖市";
        location.local = true;
        return location;
    }

    public static Location buildLocation(LocationEntity entity) {
        Location location = new Location();
        location.cityId = entity.cityId;
        location.city = entity.city;
        location.cnty = entity.cnty;
        location.lat = entity.lat;
        location.lon = entity.lon;
        location.prov = entity.prov;
        location.local = entity.local;
        return location;
    }
/*
    public static Location buildLocation(CityEntity entity) {
        Location location = new Location();
        location.cityId = entity.cityId;
        location.city = entity.city;
        location.cnty = entity.cnty;
        location.lat = entity.lat;
        location.lon = entity.lon;
        location.prov = entity.prov;
        return location;
    }

    public static Location buildLocation(OverseaCityEntity entity) {
        Location location = new Location();
        location.cityId = entity.cityId;
        location.city = entity.cityEn;
        location.cnty = entity.countryEn;
        location.lat = entity.lat;
        location.lon = entity.lon;
        location.prov = "";
        return location;
    }
*/
    public static List<Location> buildLocationListByAccuResult(List<AccuLocationResult> resultList) {
        List<Location> locationList = new ArrayList<>(resultList.size());
        for (int i = 0; i < resultList.size(); i ++) {
            Location location = new Location();
            location.cityId = resultList.get(i).Key;
            location.city = resultList.get(i).LocalizedName;
            location.cnty = resultList.get(i).Country.LocalizedName;
            location.prov = resultList.get(i).AdministrativeArea.LocalizedName;
            location.lat = String.valueOf(resultList.get(i).GeoPosition.Latitude);
            location.lon = String.valueOf(resultList.get(i).GeoPosition.Longitude);
            locationList.add(location);
        }
        return locationList;
    }

    public static List<Location> buildLocationListByCNWeather(List<CNCityList.CNCity> cityList) {
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < cityList.size(); i ++) {
            Location location = new Location();
            location.cityId = cityList.get(i).id;
            location.city = cityList.get(i).name;
            location.prov = cityList.get(i).province_name;
            location.cnty = "中国";
            location.local = false;
            locationList.add(location);
        }
        return locationList;
    }

    public static List<Location> buildLocationList(AccuLocationResult result) {
        List<Location> locationList = new ArrayList<>();
        Location location = new Location();
        location.cityId = result.Key;
        location.city = result.LocalizedName;
        location.cnty = result.Country.LocalizedName;
        location.prov = result.AdministrativeArea.LocalizedName;
        location.lat = String.valueOf(result.GeoPosition.Latitude);
        location.lon = String.valueOf(result.GeoPosition.Longitude);
        locationList.add(location);
        return locationList;
    }

    public static List<Location> buildLocationList(CNCityList.CNCity city) {
        List<Location> locationList = new ArrayList<>();
        Location location = new Location();
        location.cityId = city.id;
        location.city = city.name;
        location.prov = city.province_name;
        location.cnty = "中国";
        location.local = false;
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
/*
    public boolean isEngLocation() {
        return checkEveryCharIsEnglish(city);
    }

    public static boolean checkEveryCharIsEnglish(String txt) {
        for (int i = 0; i < txt.length(); i = i + 1) {
            if (Pattern.compile("[\u4e00-\u9fa5]")
                    .matcher(
                            String.valueOf(txt.charAt(i))).find()) {
                return false;
            }
        }
        return true;
    }
*/
    public boolean isUsable() {
        return !cityId.equals(NULL_ID);
    }

    public String getCityId() {/*
        String realId = cityId
                .replace("A", "").replace("B", "").replace("C", "").replace("D", "").replace("E", "")
                .replace("F", "").replace("G", "").replace("H", "").replace("I", "").replace("J", "")
                .replace("K", "").replace("L", "").replace("M", "").replace("N", "").replace("O", "")
                .replace("P", "").replace("Q", "").replace("R", "").replace("S", "").replace("T", "")
                .replace("U", "").replace("V", "").replace("W", "").replace("X", "").replace("Y", "")
                .replace("Z", "").replace("a", "").replace("b", "").replace("c", "").replace("d", "")
                .replace("e", "").replace("f", "").replace("g", "").replace("h", "").replace("i", "")
                .replace("j", "").replace("k", "").replace("l", "").replace("m", "").replace("n", "")
                .replace("o", "").replace("p", "").replace("q", "").replace("r", "").replace("s", "")
                .replace("t", "").replace("u", "").replace("v", "").replace("w", "").replace("x", "")
                .replace("y", "").replace("z", "");*/
        return cityId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cityId);
        dest.writeString(this.city);
        dest.writeString(this.cnty);
        dest.writeString(this.lat);
        dest.writeString(this.lon);
        dest.writeString(this.prov);
        dest.writeByte(this.local ? (byte) 1 : (byte) 0);
    }

    protected Location(Parcel in) {
        this.cityId = in.readString();
        this.city = in.readString();
        this.cnty = in.readString();
        this.lat = in.readString();
        this.lon = in.readString();
        this.prov = in.readString();
        this.local = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
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

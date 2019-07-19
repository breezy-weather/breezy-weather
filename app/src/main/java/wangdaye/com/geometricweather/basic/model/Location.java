package wangdaye.com.geometricweather.basic.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.entity.LocationEntity;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Location.
 * */

public class Location
        implements Parcelable {

    public String cityId;

    public String lat;
    public String lon;

    public String district;
    public String city;
    public String province;
    public String country;

    public String source;

    @Nullable public Weather weather;
    @Nullable public History history;

    public boolean currentPosition;
    public boolean china;

    private static final String NULL_ID = "NULL_ID";
    public static final String CURRENT_POSITION_ID = "CURRENT_POSITION";

    public Location(String cityId,
                    String district, String city, String province, String country,
                    String lat, String lon,
                    String source,
                    @Nullable Weather weather, @Nullable History history,
                    boolean currentPosition, boolean china) {
        this.cityId = cityId;
        this.district = district;
        this.city = city;
        this.province = province;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.source = source;
        this.weather = weather;
        this.history = history;
        this.currentPosition = currentPosition;
        this.china = china;
    }

    public static Location buildLocal() {
        return new Location(
                NULL_ID,
                "", "", "", "",
                "", "",
                "accu",
                null, null,
                true, false
        );
    }

    public static Location buildDefaultLocation() {
        return new Location(
                "101924",
                "", "北京", "直辖市", "中国",
                "39.904000", "116.391000",
                "accu",
                null, null,
                true, true
        );
    }

    public LocationEntity toLocationEntity() {
        LocationEntity entity = new LocationEntity();
        entity.cityId = cityId;
        entity.district = district;
        entity.city = city;
        entity.province = province;
        entity.country = country;
        entity.lat = lat;
        entity.lon = lon;
        entity.source = source;
        entity.local = currentPosition;
        entity.china = china;
        return entity;
    }

    public boolean equals(Location location) {
        if (location.isCurrentPosition()) {
            return isCurrentPosition();
        } else {
            return cityId.equals(location.cityId)
                    && source.equals(location.source);
        }
    }

    public String getFormattedId() {
        return isCurrentPosition() ? CURRENT_POSITION_ID : cityId;
    }

    public Location setCurrentPosition() {
        currentPosition = true;
        return this;
    }

    public boolean isCurrentPosition() {
        return currentPosition;
    }

    public static boolean isLocal(String formattedId) {
        return CURRENT_POSITION_ID.equals(formattedId);
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
        } else if (currentPosition) {
            return context.getString(R.string.current_location);
        } else {
            return "";
        }
    }

    public boolean hasGeocodeInformation() {
        return !TextUtils.isEmpty(country)
                || !TextUtils.isEmpty(province)
                || !TextUtils.isEmpty(city)
                || !TextUtils.isEmpty(district);
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
        dest.writeByte(this.currentPosition ? (byte) 1 : (byte) 0);
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
        this.currentPosition = in.readByte() != 0;
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

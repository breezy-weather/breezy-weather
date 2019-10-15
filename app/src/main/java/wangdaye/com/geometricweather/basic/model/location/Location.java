package wangdaye.com.geometricweather.basic.model.location;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.TimeZone;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.utils.LanguageUtils;

/**
 * Location.
 * */

public class Location
        implements Parcelable {

    private String cityId;

    private float latitude;
    private float longitude;
    private TimeZone timeZone;

    private String country;
    private String province;
    private String city;
    private String district;

    @Nullable private Weather weather;
    private WeatherSource weatherSource;

    private boolean currentPosition;
    private boolean residentPosition;
    private boolean china;

    private static final String NULL_ID = "NULL_ID";
    public static final String CURRENT_POSITION_ID = "CURRENT_POSITION";

    public Location(String cityId,
                    float latitude, float longitude, TimeZone timeZone,
                    String country, String province, String city, String district,
                    @Nullable Weather weather, WeatherSource weatherSource,
                    boolean currentPosition, boolean residentPosition, boolean china) {
        this.cityId = cityId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timeZone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.weather = weather;
        this.weatherSource = weatherSource;
        this.currentPosition = currentPosition;
        this.residentPosition = residentPosition;
        this.china = china;
    }

    public static Location buildLocal() {
        return new Location(
                NULL_ID,
                0, 0, TimeZone.getDefault(),
                "", "", "", "",
                null, WeatherSource.ACCU,
                true, false, false
        );
    }

    public static Location buildDefaultLocation() {
        return new Location(
                "101924",
                39.904000f, 116.391000f, TimeZone.getTimeZone("Asia/Shanghai"),
                "中国", "直辖市", "北京", "",
                null, WeatherSource.ACCU,
                false, false, true
        );
    }

    public void updateLocationResult(float latitude, float longitude, TimeZone timeZone,
                                     String country, String province, String city, String district,
                                     boolean china) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timeZone;
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.currentPosition = true;
        this.china = china;
    }

    public boolean equals(Location location) {
        if (location.isCurrentPosition()) {
            return isCurrentPosition();
        } else {
            return cityId.equals(location.cityId)
                    && weatherSource == location.weatherSource;
        }
    }

    public boolean equals(@Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)) {
            return false;
        }
        if (CURRENT_POSITION_ID.equals(formattedId)) {
            return isCurrentPosition();
        }
        try {
            String[] keys = formattedId.split("&");
            return cityId.equals(keys[0])
                    && weatherSource.name().equals(keys[1]);
        } catch (Exception e) {
            return false;
        }
    }

    public String getFormattedId() {
        return isCurrentPosition() ? CURRENT_POSITION_ID : (cityId + "&" + weatherSource.name());
    }

    public Location setCurrentPosition() {
        currentPosition = true;
        return this;
    }

    public boolean isCurrentPosition() {
        return currentPosition;
    }

    public boolean isResidentPosition() {
        return residentPosition;
    }

    public void setResidentPosition(boolean residentPosition) {
        this.residentPosition = residentPosition;
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

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
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

    @Nullable
    public Weather getWeather() {
        return weather;
    }

    public void setWeather(@Nullable Weather weather) {
        this.weather = weather;
    }

    public void setWeatherSource(WeatherSource source) {
        this.weatherSource = source;
    }

    public WeatherSource getWeatherSource() {
        return weatherSource;
    }

    public boolean isChina() {
        return china;
    }

    public boolean isCloseTo(Context c, Location location) {
        boolean sameId = cityId.equals(location.getCityId());
        boolean sameCity = isEquals(province, location.province)
                && isEquals(city, location.city);
        boolean sameName = isEquals(province, location.province)
                && getCityName(c).equals(location.getCityName(c));
        boolean validGeoPosition = Math.abs(latitude - location.latitude) < 0.8
                && Math.abs(longitude - location.longitude) < 0.8;
        return sameId || sameCity || sameName || validGeoPosition;
    }

    private static boolean isEquals(@Nullable String a, @Nullable String b) {
        if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
            return true;
        } else if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cityId);
        dest.writeFloat(this.latitude);
        dest.writeFloat(this.longitude);
        dest.writeSerializable(this.timeZone);
        dest.writeString(this.country);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.district);
        dest.writeInt(this.weatherSource == null ? -1 : this.weatherSource.ordinal());
        dest.writeByte(this.currentPosition ? (byte) 1 : (byte) 0);
        dest.writeByte(this.residentPosition ? (byte) 1 : (byte) 0);
        dest.writeByte(this.china ? (byte) 1 : (byte) 0);
    }

    protected Location(Parcel in) {
        this.cityId = in.readString();
        this.latitude = in.readFloat();
        this.longitude = in.readFloat();
        this.timeZone = (TimeZone) in.readSerializable();
        this.country = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.district = in.readString();
        int tmpWeatherSource = in.readInt();
        this.weatherSource = tmpWeatherSource == -1 ? null : WeatherSource.values()[tmpWeatherSource];
        this.currentPosition = in.readByte() != 0;
        this.residentPosition = in.readByte() != 0;
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

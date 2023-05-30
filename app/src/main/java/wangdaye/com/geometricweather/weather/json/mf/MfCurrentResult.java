package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Mf current result.
 **/

public class MfCurrentResult {
    @SerializedName("update_time")
    public Date updateTime;
    public String type;
    public Geometry geometry;
    public Properties properties;

    public static class Geometry {
        public String type;
        public List<Float> coordinates;
    }

    public static class Properties {
        public String timezone;
        public Gridded gridded;

        public static class Gridded {
            public Date time;
            @SerializedName("T")
            public Float temperature;
            @SerializedName("wind_speed")
            public Float windSpeed;
            @SerializedName("wind_direction")
            public Integer windDirection;
            @SerializedName("wind_icon")
            public String windIcon;
            @SerializedName("weather_icon")
            public String weatherIcon;
            @SerializedName("weather_description")
            public String weatherDescription;
        }
    }
}
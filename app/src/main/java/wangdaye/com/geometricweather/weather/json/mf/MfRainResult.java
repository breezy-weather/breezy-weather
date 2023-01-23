package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MfRainResult {
    @SerializedName("update_time")
    public long updateTime;
    public Geometry geometry;
    public ForecastProperties properties;

    public static class Geometry {
        public List<Float> coordinates;
        public String type;
    }

    public static class ForecastProperties {
        public Integer altitude;
        public String name;
        public String country;
        @SerializedName("french_department")
        public String frenchDepartment;
        @SerializedName("rain_product_available")
        public Integer rainProductAvailable;
        public String timezone;

        @SerializedName("forecast")
        public List<RainForecast> rainForecasts;

        public static class RainForecast {
            public long time;
            @SerializedName("rain_intensity")
            public int rainIntensity;
            @SerializedName("rain_intensity_description")
            public String rainIntensityDescription;
        }
    }
}
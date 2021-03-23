package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MfRainResult {
    public Position position;
    public int quality;
    @SerializedName("forecast")
    public List<RainForecast> rainForecasts;
    @SerializedName("updated_on")
    public long updatedOn;

    public static class Position {
        public Integer alti;
        public String country;
        public String dept;
        @SerializedName("rain_product_available")
        public int hasRain;
        @SerializedName("bulletin_cote")
        public int hasSeaBulletin;
        public String insee;
        public double lat;
        public double lon;
        public String name;
        public String timezone;
    }

    public static class RainForecast {
        @SerializedName("dt")
        public long date;
        public String desc;
        public int rain;
    }
}
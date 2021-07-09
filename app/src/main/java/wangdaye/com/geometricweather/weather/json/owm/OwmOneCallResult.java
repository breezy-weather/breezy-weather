package wangdaye.com.geometricweather.weather.json.owm;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * OpenWeather One Call result.
 * */

public class OwmOneCallResult {
    public double lat;
    public double lon;
    public String timezone;
    @SerializedName("timezone_offset")
    public int timezoneOffset;
    public Current current;
    public List<Minutely> minutely;
    public List<Hourly> hourly;
    public List<Daily> daily;
    public List<Alert> alerts;

    public static class Current {
        public long dt;
        public long sunrise;
        public long sunset;
        public double temp;
        @SerializedName("feels_like")
        public double feelsLike;
        public int pressure;
        public int humidity;
        @SerializedName("dew_point")
        public double dewPoint;
        public double uvi;
        public int clouds;
        public int visibility;
        @SerializedName("wind_speed")
        public Float windSpeed;
        @SerializedName("wind_deg")
        public int windDeg;
        public List<Weather> weather;
        public Precipitation rain;
        public Precipitation snow;
    }

    public static class Minutely {
        public long dt;
        public float precipitation;
    }

    public static class Hourly {
        public long dt;
        public double temp;
        @SerializedName("feels_like")
        public double feelsLike;
        public int pressure;
        public int humidity;
        @SerializedName("dew_point")
        public double dewPoint;
        public double uvi;
        public int clouds;
        public int visibility;
        @SerializedName("wind_speed")
        public double windSpeed;
        @SerializedName("wind_deg")
        public int windDeg;
        public List<Weather> weather;
        public Float pop;
        public Precipitation rain;
        public Precipitation snow;
    }

    public static class Precipitation {
        @SerializedName("1h")
        public Float cumul1h;
    }

    public static class Daily {
        public long dt;
        public long sunrise;
        public long sunset;
        public Temp temp;
        @SerializedName("feels_like")
        public FeelsLike feelsLike;
        public int pressure;
        public int humidity;
        @SerializedName("dew_point")
        public double dewPoint;
        @SerializedName("wind_speed")
        public Float windSpeed;
        @SerializedName("wind_deg")
        public int windDeg;
        public List<Weather> weather;
        public int clouds;
        public Float pop;
        public Float rain;
        public Float snow;
        public double uvi;

        public static class Temp {
            public double day;
            public double min;
            public double max;
            public double night;
            public double eve;
            public double morn;
        }
        public static class FeelsLike {
            public double day;
            public double night;
            public double eve;
            public double morn;
        }
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Alert {
        @SerializedName("sender_name")
        public String senderName;
        public String event;
        public long start;
        public long end;
        public String description;
    }
}

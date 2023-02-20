package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mf forecast result.
 * */

public class MfForecastV2Result {
    public Geometry geometry;
    public ForecastProperties properties;
    public String type;
    @SerializedName("update_time")
    public Long updateTime;

    public static class Geometry {
        public List<Float> coordinates;
        public String type;
    }

    public static class ForecastProperties {
        public Integer altitude;
        @SerializedName("bulletin_cote")
        public Integer bulletinCote;
        public String country;
        @SerializedName("daily_forecast")
        public List<ForecastV2> dailyForecast;
        public List<HourForecast> forecast;
        @SerializedName("french_department")
        public String frenchDepartment;
        public String insee;
        public String name;
        @SerializedName("probability_forecast")
        public List<ProbabilityForecastV2> probabilityForecast;
        @SerializedName("rain_product_available")
        public Integer rainProductAvailable;
        public String timezone;

        public static class ForecastV2 {
            @SerializedName("daily_weather_description")
            public String dailyWeatherDescription;
            @SerializedName("daily_weather_icon")
            public String dailyWeatherIcon;
            @SerializedName("relative_humidity_max")
            public Integer relativeHumidityMax;
            @SerializedName("relative_humidity_min")
            public Integer relativeHumidityMin;
            @SerializedName("sunrise_time")
            public Long sunriseTime;
            @SerializedName("sunset_time")
            public Long sunsetTime;
            @SerializedName("T_max")
            public Float tMax;
            @SerializedName("T_min")
            public Float tMin;
            @SerializedName("T_sea")
            public Float tSea;
            public Long time;
            @SerializedName("total_precipitation_24h")
            public Float totalPrecipitation24h;
            @SerializedName("uv_index")
            public Integer uvIndex;
        }

        public static class HourForecast {
            @SerializedName("weather_confidence_index")
            public Integer confidence;
            public Integer iso0;
            @SerializedName("moment_day")
            public String momentDay;
            @SerializedName("P_sea")
            public Float pSea;
            @SerializedName("rain_12h")
            public Float rain12h;
            @SerializedName("rain_1h")
            public Float rain1h;
            @SerializedName("rain_24h")
            public Float rain24h;
            @SerializedName("rain_3h")
            public Float rain3h;
            @SerializedName("rain_6h")
            public Float rain6h;
            @SerializedName("relative_humidity")
            public Integer relativeHumidity;
            @SerializedName("snow_12h")
            public Float snow12h;
            @SerializedName("snow_1h")
            public Float snow1h;
            @SerializedName("snow_24h")
            public Float snow24h;
            @SerializedName("snow_3h")
            public Float snow3h;
            @SerializedName("snow_6h")
            public Float snow6h;
            @SerializedName("T")
            public Float t;
            @SerializedName("T_windchill")
            public Float tWindchill;
            public Long time;
            @SerializedName("total_cloud_cover")
            public Integer totalCloudCover;
            @SerializedName("weather_description")
            public String weatherDescription;
            @SerializedName("weather_icon")
            public String weatherIcon;
            @SerializedName("wind_direction")
            public String windDirection;
            @SerializedName("wind_icon")
            public String windIcon;
            @SerializedName("wind_speed")
            public Integer windSpeed;
            @SerializedName("wind_speed_gust")
            public Integer windSpeedGust;
        }

        public static class ProbabilityForecastV2 {
            @SerializedName("freezing_hazard")
            public Integer freezingHazard;
            @SerializedName("rain_hazard_3h")
            public Integer rainHazard3h;
            @SerializedName("rain_hazard_6h")
            public Integer rainHazard6h;
            @SerializedName("snow_hazard_3h")
            public Integer snowHazard3h;
            @SerializedName("snow_hazard_6h")
            public Integer snowHazard6h;
            @SerializedName("storm_hazard")
            public Integer stormHazard;
            public Long time;
        }
    }
}
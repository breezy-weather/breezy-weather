package wangdaye.com.geometricweather.weather.json.mf;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mf forecast result.
 * */

public class MfForecastResult {
    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecasts;
    @SerializedName("forecast")
    public List<Forecast> forecasts;
    public Position position;
    @SerializedName("probability_forecast")
    public List<ProbabilityForecast> probabilityForecast;
    @SerializedName("updated_on")
    public long updatedOn;

    public static class DailyForecast {
        public long dt;
        public Humidity humidity;
        public Precipitation precipitation;
        public Sun sun;
        @SerializedName("T")
        public DailyTemperature temperature;
        public int uv;
        @Nullable
        public Weather weather12H;

        public static class Humidity {
            public Integer max;
            public Integer min;
        }

        public static class Precipitation {
            @SerializedName("24h")
            public Float cumul24H;
        }

        public static class Sun {
            public Long rise;
            public Long set;
        }

        public static class DailyTemperature {
            public Float max;
            public Float min;
        }

        public static class Weather {
            public String desc;
            public String icon;
        }
    }

    public static class Forecast {
        public Integer clouds;
        public long dt;
        public Integer humidity;
        public Integer iso0;
        public Precipitation precipitation;
        public Rain rain;
        @SerializedName("rain snow limit")
        public Object rainSnowLimitRaw;
        @SerializedName("sea_level")
        public double seaLevel;
        public Snow snow;
        @SerializedName("T")
        public Temperature temperature;
        public Weather weather;
        public Wind wind;

        public static class Precipitation {
            @SerializedName("24h")
            public Float cumul24H;
        }

        public static class Rain {
            @SerializedName("12h")
            public Float cumul12H;
            @SerializedName("1h")
            public Float cumul1H;
            @SerializedName("24h")
            public Float cumul24H;
            @SerializedName("3h")
            public Float cumul3H;
            @SerializedName("6h")
            public Float cumul6H;
        }

        public static class Snow {
            @SerializedName("12h")
            public Float cumul12H;
            @SerializedName("1h")
            public Float cumul1H;
            @SerializedName("24h")
            public Float cumul24H;
            @SerializedName("3h")
            public Float cumul3H;
            @SerializedName("6h")
            public Float cumul6H;
        }

        public static class Temperature {
            public Float value;
            @SerializedName("windchill")
            public Float windChill;
        }

        public static class Weather {
            public String desc;
            public String icon;
        }

        public static class Wind {
            public Integer direction;
            public Integer gust;
            public String icon;
            public Integer speed;
        }
    }

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

    public static class ProbabilityForecast {
        public long dt;
        public Integer freezing;
        public ProbabilityRain rain;
        public ProbabilitySnow snow;

        public static class ProbabilityRain {
            @SerializedName("3h")
            public Integer proba3H;
            @SerializedName("6h")
            public Integer proba6H;
        }

        public static class ProbabilitySnow {
            @SerializedName("3h")
            public Integer proba3H;
            @SerializedName("6h")
            public Integer proba6H;
        }
    }
}
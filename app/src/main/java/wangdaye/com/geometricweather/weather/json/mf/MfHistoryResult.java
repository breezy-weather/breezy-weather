package wangdaye.com.geometricweather.weather.json.mf;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Mf history result.
 **/

public class MfHistoryResult {
    public Position position;
    public List<History> history;

    public static class Position {
        public double lat;
        public double lon;
        public Integer alti;
        public String name;
        public String country;
        public String dept;
        public String timezone;
    }

    public static class History {
        public long dt;
        @SerializedName("T")
        public Temperature temperature;
        public int humidity;
        @SerializedName("sea_level")
        public double seaLevel;
        public double visibility;
        public Wind wind;
        @Nullable
        public Precipitation precipitation;
        public Snow snow;
        public Integer clouds;
        public Weather weather;

        public static class Temperature {
            public Float value;
            @SerializedName("windchill")
            public Float windChill;
        }

        public static class Wind {
            public double speed;
            public double gust;
            public Integer direction;
            public String icon;
        }

        public static class Precipitation {
            @SerializedName("1h")
            public double qty1H;
            @SerializedName("3h")
            public double qty3H;
            @SerializedName("6h")
            public double qty6H;
            @SerializedName("12h")
            public double qty12H;
            @SerializedName("24h")
            public double qty24H;
        }

        public static class Snow {
            public Integer depth;
            public Integer fresh;
        }

        public static class Weather {
            public String desc;
            public String icon;
        }
    }
}
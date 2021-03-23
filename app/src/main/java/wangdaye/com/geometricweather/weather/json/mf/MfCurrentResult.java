package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

/**
 * Mf current result.
 **/

public class MfCurrentResult {
    public Position position;
    @SerializedName("updated_on")
    public long updatedOn;
    public Observation observation;

    public static class Position {
        public double lat;
        public double lon;
        public String timezone;
    }

    public static class Observation {
        @SerializedName("T")
        public Float temperature;
        public Wind wind;
        public Weather weather;

        public static class Wind {
            public double speed;
            public double gust;
            public Integer direction;
            public String icon;
        }

        public static class Weather {
            public String desc;
            public String icon;
        }
    }
}
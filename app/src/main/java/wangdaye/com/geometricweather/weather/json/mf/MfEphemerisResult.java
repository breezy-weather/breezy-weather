package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

public class MfEphemerisResult {
    public String type;
    public Geometry geometry;
    public Properties properties;

    public static class Geometry {
        public String type;
        //coordinates
    }

    public static class Properties {
        public Ephemeris ephemeris;

        public static class Ephemeris {
            @SerializedName("sunrise_time")
            public long sunriseTime;
            @SerializedName("sunset_time")
            public long sunsetTime;
            @SerializedName("moonrise_time")
            public long moonriseTime;
            @SerializedName("moonset_time")
            public long moonsetTime;
            @SerializedName("moon_phase")
            public String moonPhase;
            @SerializedName("moon_phase_description")
            public String moonPhaseDescription;
            public String saint;
        }
    }
}
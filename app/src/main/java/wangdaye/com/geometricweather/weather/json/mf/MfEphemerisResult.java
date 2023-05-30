package wangdaye.com.geometricweather.weather.json.mf;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

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
            public Date sunriseTime;
            @SerializedName("sunset_time")
            public Date sunsetTime;
            @SerializedName("moonrise_time")
            public Date moonriseTime;
            @SerializedName("moonset_time")
            public Date moonsetTime;
            @SerializedName("moon_phase")
            public String moonPhase;
            @SerializedName("moon_phase_description")
            public String moonPhaseDescription;
            public String saint;
        }
    }
}
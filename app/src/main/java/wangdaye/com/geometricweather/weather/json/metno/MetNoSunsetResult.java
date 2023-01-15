package wangdaye.com.geometricweather.weather.json.metno;

import java.util.Date;
import java.util.List;

/**
 * MET Norway sun/moon rise/set forecast.
 **/
public class MetNoSunsetResult {
    public Location location;

    public static class Location {
        public List<Time> time;

        public static class Time {
            public String date;
            public MoonPosition moonposition;
            public Phase moonrise;
            public Phase moonset;
            public Phase sunrise;
            public Phase sunset;

            public static class MoonPosition {
                public Float phase;
                public String desc;
            }

            public static class Phase {
                public String desc;
                public Date time;
            }
        }
    }
}

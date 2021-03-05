package wangdaye.com.geometricweather.weather.json.owm;

import java.util.List;

/**
 * OpenWeather Air Pollution result.
 * */

public class OwmAirPollutionResult {
    public double[] coord;
    public List<AirPollution> list;

    public static class AirPollution {
        public long dt;
        public Main main;
        public Components components;

        public static class Main {
            public int aqi;
        }

        public static class Components {
            public double co;
            public double no;
            public double no2;
            public double o3;
            public double so2;
            public double pm2_5;
            public double pm10;
            public double nh3;
        }
    }
}

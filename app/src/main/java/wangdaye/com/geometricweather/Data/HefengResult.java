package wangdaye.com.geometricweather.Data;

import java.util.List;

/**
 * Created by WangDaYe on 2016/2/21.
 */
public class HefengResult {
    public List<HeWeather> heWeather;

    public static class HeWeather {
        public Aqi aqi;
        public Basic basic;
        public List<Daily_forecast> daily_forecast;
        public List<Hourly_forecast> hourly_forecast;
        public Now now;
        public String status;
    }

    public static class Aqi {
        public String aqi;
        public String co;
        public String no2;
        public String o3;
        public String pm10;
        public String pm25;
        public String qlty;
        public String so2;
    }

    public static class Basic {
        public String city;
        public String cnty;
        public String id;
        public String lat;
        public String lon;
        public Update update;
    }

    public static class Update {
        public String loc;
        public String utc;
    }

    public static class Daily_forecast {
        public Astro astro;
        public Cond cond;
        public String date;
        public String hum;
        public String pcpn;
        public String pop;
        public String pres;
        public Tmp tmp;
        public String vis;
        public Wind wind;
    }

    public static class Astro {
        public String sr;
        public String ss;
    }

    public static class Cond {
        public String code_d;
        public String code_n;
        public String txt_d;
        public String txt_n;
    }

    public static class Tmp {
        public String max;
        public String min;
    }

    public static class Wind {
        public String deg;
        public String dir;
        public String sc;
        public String spd;
    }

    public static class Hourly_forecast {
        public String date;
        public String hum;
        public String pop;
        public String pres;
        public String tmp;
        public Wind wind;
    }

    public static class Now {
        public CondNow condNow;
        public String fl;
        public String hum;
        public String pcpn;
        public String pres;
        public String tmp;
        public String vis;
        public Wind wind;
    }

    public static class CondNow {
        public String code;
        public String txt;
    }
}

package wangdaye.com.geometricweather.Data;

import java.util.List;

/**
 * The google gson's result from juhe.cn.
 * */

public class JuheResult {

    public String reason;
    public Result result;
    public String error_code;

    public static class Result {
        public Data data;
    }

    public static class Data {
        public Realtime realtime;
        public Life life;
        public List<Weather> weather;
        public Air air;
        public String date;
        public String isForeign;
    }

    public static class Realtime {
        public String city_code;
        public String city_name;
        public String date;
        public String time;
        public String week;
        public String moon;
        public String dataUptime;
        public WeatherNow weatherNow;
        public Wind wind;
    }

    public static class WeatherNow {
        public String temperature;
        public String humidity;
        public String weatherInfo;
        public String img;
    }

    public static class Wind {
        public String direct;
        public String power;
        public String offset;
        public String windspeed;
    }

    public static class Life {
        public String data;
        public LifeInfo lifeInfo;
    }

    public static class LifeInfo {
        public List<String> chuanyi;
        public List<String> ganmao;
        public List<String> kongtiao;
        public List<String> wuran;
        public List<String> xiche;
        public List<String> yundong;
        public List<String> ziwaixian;
    }

    public static class Weather {
        public String date;
        public Info info;
        public String week;
        public String nongli;
    }

    public static class Info {
        public List<String> day;
        public List<String> night;
    }

    public static class Air {
        public String key;
        public String show_desc;
        public Pm25 pm25;
        public String dateTime;
        public String cityName;
    }

    public static class Pm25 {
        public String curPm;
        public String pm25;
        public String pm10;
        public String level;
        public String quality;
        public String des;
    }
}

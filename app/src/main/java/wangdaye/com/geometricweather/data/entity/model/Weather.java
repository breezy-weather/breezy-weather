package wangdaye.com.geometricweather.data.entity.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.table.WeatherEntity;
import wangdaye.com.geometricweather.data.entity.result.HefengResult;
import wangdaye.com.geometricweather.data.entity.result.JuheResult;
import wangdaye.com.geometricweather.data.service.HefengWeather;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather
 * */

public class Weather {
    // data
    public static final int DAILY_LENGTH = 5;
    public static final int HOURLY_LENGTH = 8;

    public Base base;
    public Live live;
    public List<Daily> dailyList;
    public List<Hourly> hourlyList;
    public Life life;

    public static class Base {
        public String location;
        public String refreshTime;

        public String date;
        public String moon;
        public String week;
    }

    public static class Live {
        public String weather;
        public String weatherKind;
        public int temp;
        public String air;

        public String windDir;
        public String windLevel;
    }

    public static class Daily {
        public String date;
        public String week;
        public String[] weathers;
        public String[] weatherKinds;
        public int[] temps;
        public String[] windDirs;
        public String[] windLevels;
        public String[] astros;
    }

    public static class Hourly {
        public String hour;
        public int temp;
        public float pop;
    }

    public static class Life {
        public String[] winds;
        public String[] pms;
        public String[] hums;
        public String[] uvs;
        public String[] dresses;
        public String[] colds;
        public String[] airs;
        public String[] washCars;
        public String[] sports;
    }

    /** <br> life cycle. */

    public static Weather build(WeatherEntity entity) {
        Weather w = new Weather();

        w.base = new Base();
        w.base.location = entity.location;
        w.base.refreshTime = entity.refreshTime;
        w.base.date = entity.date;
        w.base.moon = entity.moon;
        w.base.week = entity.week;

        w.live = new Live();
        w.live.weather = entity.weatherNow;
        w.live.weatherKind = entity.weatherKindNow;
        w.live.temp = entity.tempNow;
        w.live.air = entity.airNow;
        w.live.windDir = entity.windDirNow;
        w.live.windLevel = entity.windLevelNow;

        w.dailyList = new ArrayList<>();

        Daily dailies_1 = new Daily();
        dailies_1.date = entity.dates_1;
        dailies_1.week = entity.weeks_1;
        dailies_1.weathers = new String[] {
                entity.dayWeathers_1,
                entity.nightWeathers_1};
        dailies_1.temps = new int[] {
                entity.dayTemps_1,
                entity.nightTemps_1};
        dailies_1.windDirs = new String[] {
                entity.dayWindDirs_1,
                entity.nightWindDirs_1};
        dailies_1.windLevels = new String[] {
                entity.dayWindLevels_1,
                entity.nightWindLevels_1};
        dailies_1.astros = new String[] {
                entity.sunriseTimes_1,
                entity.sunsetTimes_1};
        w.dailyList.add(dailies_1);

        Daily dailies_2 = new Daily();
        dailies_2.date = entity.dates_2;
        dailies_2.week = entity.weeks_2;
        dailies_2.weathers = new String[] {
                entity.dayWeathers_2,
                entity.nightWeathers_2};
        dailies_2.temps = new int[] {
                entity.dayTemps_2,
                entity.nightTemps_2};
        dailies_2.windDirs = new String[] {
                entity.dayWindDirs_2,
                entity.nightWindDirs_2};
        dailies_2.windLevels = new String[] {
                entity.dayWindLevels_2,
                entity.nightWindLevels_2};
        dailies_2.astros = new String[] {
                entity.sunriseTimes_2,
                entity.sunsetTimes_2};
        w.dailyList.add(dailies_2);

        Daily dailies_3 = new Daily();
        dailies_3.date = entity.dates_3;
        dailies_3.week = entity.weeks_3;
        dailies_3.weathers = new String[] {
                entity.dayWeathers_3,
                entity.nightWeathers_3};
        dailies_3.temps = new int[] {
                entity.dayTemps_3,
                entity.nightTemps_3};
        dailies_3.windDirs = new String[] {
                entity.dayWindDirs_3,
                entity.nightWindDirs_3};
        dailies_3.windLevels = new String[] {
                entity.dayWindLevels_3,
                entity.nightWindLevels_3};
        dailies_3.astros = new String[] {
                entity.sunriseTimes_3,
                entity.sunsetTimes_3};
        w.dailyList.add(dailies_3);

        Daily dailies_4 = new Daily();
        dailies_4.date = entity.dates_4;
        dailies_4.week = entity.weeks_4;
        dailies_4.weathers = new String[] {
                entity.dayWeathers_4,
                entity.nightWeathers_4};
        dailies_4.temps = new int[] {
                entity.dayTemps_4,
                entity.nightTemps_4};
        dailies_4.windDirs = new String[] {
                entity.dayWindDirs_4,
                entity.nightWindDirs_4};
        dailies_4.windLevels = new String[] {
                entity.dayWindLevels_4,
                entity.nightWindLevels_4};
        dailies_4.astros = new String[] {
                entity.sunriseTimes_4,
                entity.sunsetTimes_4};
        w.dailyList.add(dailies_4);

        Daily dailies_5 = new Daily();
        dailies_5.date = entity.dates_5;
        dailies_5.week = entity.weeks_5;
        dailies_5.weathers = new String[] {
                entity.dayWeathers_5,
                entity.nightWeathers_5};
        dailies_5.temps = new int[] {
                entity.dayTemps_5,
                entity.nightTemps_5};
        dailies_5.windDirs = new String[] {
                entity.dayWindDirs_5,
                entity.nightWindDirs_5};
        dailies_5.windLevels = new String[] {
                entity.dayWindLevels_5,
                entity.nightWindLevels_5};
        dailies_5.astros = new String[] {
                entity.sunriseTimes_5,
                entity.sunsetTimes_5};
        w.dailyList.add(dailies_5);

        w.hourlyList = new ArrayList<>();

        Hourly hourlies_1 = new Hourly();
        hourlies_1.hour = entity.hours_1;
        hourlies_1.temp = entity.hourlyTemps_1;
        hourlies_1.pop = entity.hourlyPops_1;
        w.hourlyList.add(hourlies_1);

        Hourly hourlies_2 = new Hourly();
        hourlies_2.hour = entity.hours_2;
        hourlies_2.temp = entity.hourlyTemps_2;
        hourlies_2.pop = entity.hourlyPops_2;
        w.hourlyList.add(hourlies_2);

        Hourly hourlies_3 = new Hourly();
        hourlies_3.hour = entity.hours_3;
        hourlies_3.temp = entity.hourlyTemps_3;
        hourlies_3.pop = entity.hourlyPops_3;
        w.hourlyList.add(hourlies_3);

        Hourly hourlies_4 = new Hourly();
        hourlies_4.hour = entity.hours_4;
        hourlies_4.temp = entity.hourlyTemps_4;
        hourlies_4.pop = entity.hourlyPops_4;
        w.hourlyList.add(hourlies_4);

        Hourly hourlies_5 = new Hourly();
        hourlies_5.hour = entity.hours_5;
        hourlies_5.temp = entity.hourlyTemps_5;
        hourlies_5.pop = entity.hourlyPops_5;
        w.hourlyList.add(hourlies_5);

        Hourly hourlies_6 = new Hourly();
        hourlies_6.hour = entity.hours_6;
        hourlies_6.temp = entity.hourlyTemps_6;
        hourlies_6.pop = entity.hourlyPops_6;
        w.hourlyList.add(hourlies_6);

        Hourly hourlies_7 = new Hourly();
        hourlies_7.hour = entity.hours_7;
        hourlies_7.temp = entity.hourlyTemps_7;
        hourlies_7.pop = entity.hourlyPops_7;
        w.hourlyList.add(hourlies_7);

        Hourly hourlies_8 = new Hourly();
        hourlies_8.hour = entity.hours_8;
        hourlies_8.temp = entity.hourlyTemps_8;
        hourlies_8.pop = entity.hourlyPops_8;
        w.hourlyList.add(hourlies_8);

        w.life = new Life();
        w.life.winds = new String[] {entity.winds_1, entity.winds_2};
        w.life.pms = new String[] {entity.pms_1, entity.pms_2};
        w.life.hums = new String[] {entity.hums_1, entity.hums_2};
        w.life.uvs = new String[] {entity.uvs_1, entity.uvs_2};
        w.life.dresses = new String[] {entity.dresses_1, entity.dresses_2};
        w.life.colds = new String[] {entity.colds_1, entity.colds_2};
        w.life.airs = new String[] {entity.airs_1, entity.airs_2};
        w.life.washCars = new String[] {entity.washCars_1, entity.washCars_2};
        w.life.sports = new String[] {entity.sports_1, entity.sports_2};

        return w;
    }

    public static Weather build(Context c, JuheResult result) {
        if (result.result == null || result.error_code != 0) {
            return null;
        }

        try {
            Weather w = new Weather();

            w.base = new Base();
            w.base.location = result.result.data.realtime.city_name;
            w.base.refreshTime = result.result.data.realtime.time.split(":")[0]
                    + ":" + result.result.data.realtime.time.split(":")[1];
            w.base.date = result.result.data.realtime.date;
            w.base.moon = result.result.data.realtime.moon;
            w.base.week = c.getString(R.string.week) + result.result.data.weather.get(0).week;

            w.live = new Live();
            w.live.weather = result.result.data.realtime.weather.info;
            w.live.weatherKind = WeatherHelper.getJuheWeatherKind(result.result.data.realtime.weather.img);
            w.live.temp = Integer.parseInt(result.result.data.realtime.weather.temperature);
            if (result.result.data.aqi != null) {
                w.live.air = c.getString(R.string.air) + " " + result.result.data.aqi.pm25.quality;
            } else {
                w.live.air = "";
            }
            w.live.windDir = result.result.data.realtime.wind.direct;
            w.live.windLevel = result.result.data.realtime.wind.power;

            w.dailyList = new ArrayList<>();
            for (int i = 0; i < DAILY_LENGTH; i ++) {
                if (i < result.result.data.weather.size()) {
                    Daily daily = new Daily();
                    daily.date = result.result.data.weather.get(i).date;
                    daily.week = c.getString(R.string.week) + result.result.data.weather.get(i).week;
                    daily.weathers = new String[] {
                            result.result.data.weather.get(i).info.day.get(1),
                            result.result.data.weather.get(i).info.night.get(1)};
                    daily.weatherKinds = new String[] {
                            WeatherHelper.getJuheWeatherKind(result.result.data.weather.get(i).info.day.get(0)),
                            WeatherHelper.getJuheWeatherKind(result.result.data.weather.get(i).info.night.get(0))
                    };
                    daily.temps = new int[] {
                            Integer.parseInt(result.result.data.weather.get(i).info.day.get(2)),
                            Integer.parseInt(result.result.data.weather.get(i).info.night.get(2))};
                    daily.windDirs = new String[] {
                            result.result.data.weather.get(i).info.day.get(3),
                            result.result.data.weather.get(i).info.night.get(3)};
                    daily.windLevels = new String[] {
                            result.result.data.weather.get(i).info.day.get(4),
                            result.result.data.weather.get(i).info.night.get(4)};
                    daily.astros = new String[] {
                            result.result.data.weather.get(i).info.day.get(5),
                            result.result.data.weather.get(i).info.night.get(5)};

                    w.dailyList.add(daily);
                } else {
                    Daily daily = new Daily();
                    daily.date = "null";
                    daily.week = "null";
                    daily.weathers = new String[] {"null", "null"};
                    daily.temps = new int[] {0, 0};
                    daily.windDirs = new String[] {"null", "null"};
                    daily.windLevels = new String[] {"null", "null"};
                    daily.astros = new String[] {"null", "null"};

                    w.dailyList.add(daily);
                }
            }

            w.hourlyList = new ArrayList<>();
            for (int i = 0; i < HOURLY_LENGTH; i ++) {
                if (i < result.result.data.f3h.temperature.size()) {
                    Hourly hourly = new Hourly();
                    hourly.hour = result.result.data.f3h.temperature.get(i).jg.substring(8, 10);
                    hourly.temp = Integer.parseInt(result.result.data.f3h.temperature.get(i).jb);
                    hourly.pop = (int) (Math.min(
                            100 * Float.parseFloat(result.result.data.f3h.precipitation.get(i).jf),
                            100));

                    w.hourlyList.add(hourly);
                } else {
                    Hourly hourly = new Hourly();
                    hourly.hour = "null";
                    hourly.temp = 0;
                    hourly.pop = 0;

                    w.hourlyList.add(hourly);
                }
            }

            w.life = new Life();
            // wind.
            w.life.winds = new String[] {
                    c.getString(R.string.live) + " - " + w.live.windDir + w.live.windLevel,
                    c.getString(R.string.today)
                            + c.getString(R.string.day)
                            + result.result.data.weather.get(0).info.day.get(3)
                            + result.result.data.weather.get(0).info.day.get(4)
                            + ", "
                            + c.getString(R.string.night)
                            + result.result.data.weather.get(0).info.night.get(3)
                            + result.result.data.weather.get(0).info.night.get(4)
                            + "。"};
            // pm 2.5 & pm 10.
            if (result.result.data.aqi != null) {
                w.life.pms = new String[] {
                        c.getString(R.string.pm_25) + " - " + result.result.data.aqi.pm25.pm25
                                + " / "
                                + c.getString(R.string.pm_10) + " - " + result.result.data.aqi.pm25.pm10,
                        result.result.data.aqi.pm25.des};
            } else {
                w.life.pms = new String[] {"", ""};
            }
            // humidity.
            w.life.hums = new String[] {
                    c.getString(R.string.humidity),
                    result.result.data.realtime.weather.humidity};
            // uv.
            if (result.result.data.life.info.ziwaixian != null) {
                w.life.uvs = new String[] {
                        c.getString(R.string.uv) + " - " + result.result.data.life.info.ziwaixian.get(0),
                        result.result.data.life.info.ziwaixian.get(1)};
            } else {
                w.life.uvs = new String[] {"", ""};
            }
            // dress index.
            if (result.result.data.life.info.chuanyi != null) {
                w.life.dresses = new String[] {
                        c.getString(R.string.dressing_index) + " - " + result.result.data.life.info.chuanyi.get(0),
                        result.result.data.life.info.chuanyi.get(1)};
            } else {
                w.life.dresses = new String[] {"", ""};
            }
            // cold index.
            if (result.result.data.life.info.ganmao != null) {
                w.life.colds = new String[] {
                        c.getString(R.string.cold_index) + " - " + result.result.data.life.info.ganmao.get(0),
                        result.result.data.life.info.ganmao.get(1)};
            } else {
                w.life.colds = new String[] {"", ""};
            }
            // air index.
            if (result.result.data.aqi != null) {
                w.life.airs = new String[] {
                        c.getString(R.string.aqi) + " - " + result.result.data.aqi.pm25.level + c.getString(R.string.level),
                        result.result.data.aqi.pm25.des};
            } else {
                w.life.airs = new String[] {"", ""};
            }
            // wash car index.
            if (result.result.data.life.info.xiche != null) {
                w.life.washCars = new String[] {
                        c.getString(R.string.wash_car_index) + " - " + result.result.data.life.info.xiche.get(0),
                        result.result.data.life.info.xiche.get(1)};
            } else {
                w.life.washCars = new String[] {"", ""};
            }
            // exercise index.
            if (result.result.data.life.info.yundong != null) {
                w.life.sports = new String[] {
                        c.getString(R.string.exercise_index) + " - " + result.result.data.life.info.yundong.get(0),
                        result.result.data.life.info.yundong.get(1)};
            } else {
                w.life.sports = new String[] {"", ""};
            }

            return w;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Weather build(Context c, HefengResult result) {
        int p = HefengWeather.getLatestDataPosition(result);
        if (result == null
                || result.heWeather == null || result.heWeather.size() == 0
                || !result.heWeather.get(p).status.equals("ok")) {
            return null;
        }

        try {
            Weather w = new Weather();

            w.base = new Base();
            w.base.location = result.heWeather.get(p).basic.city;
            w.base.refreshTime = result.heWeather.get(p).basic.update.loc.split(" ")[1];
            w.base.date = result.heWeather.get(p).basic.update.loc.split(" ")[0];
            w.base.moon = "";
            w.base.week = HefengWeather.getWeek(c, result.heWeather.get(p).basic.update.loc.split(" ")[0]);

            w.live = new Live();
            w.live.weather = result.heWeather.get(p).now.cond.txt;
            w.live.weatherKind = WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).now.cond.code);
            w.live.temp = Integer.parseInt(result.heWeather.get(p).now.tmp);
            if (result.heWeather.get(p).aqi != null) {
                w.live.air = c.getString(R.string.air) + " " + result.heWeather.get(p).aqi.city.qlty;
            } else {
                w.live.air = "";
            }
            w.live.windDir = result.heWeather.get(p).now.wind.dir;
            w.live.windLevel = result.heWeather.get(p).now.wind.sc + c.getString(R.string.level);

            w.dailyList = new ArrayList<>();
            for (int i = 0; i < DAILY_LENGTH; i ++) {
                if (i < result.heWeather.get(p).daily_forecast.size()) {
                    Daily daily = new Daily();
                    daily.date = result.heWeather.get(p).daily_forecast.get(i).date;
                    daily.week = HefengWeather.getWeek(c, result.heWeather.get(p).daily_forecast.get(i).date);
                    daily.weathers = new String[] {
                            result.heWeather.get(p).daily_forecast.get(i).cond.txt_d,
                            result.heWeather.get(p).daily_forecast.get(i).cond.txt_n};
                    daily.weatherKinds = new String[] {
                            WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).daily_forecast.get(i).cond.code_d),
                            WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).daily_forecast.get(i).cond.code_n)};
                    daily.temps = new int[] {
                            Integer.parseInt(result.heWeather.get(p).daily_forecast.get(i).tmp.max),
                            Integer.parseInt(result.heWeather.get(p).daily_forecast.get(i).tmp.min)};
                    daily.windDirs = new String[] {
                            result.heWeather.get(p).daily_forecast.get(i).wind.dir,
                            result.heWeather.get(p).daily_forecast.get(i).wind.dir};
                    daily.windLevels = new String[] {
                            result.heWeather.get(p).daily_forecast.get(i).wind.sc,
                            result.heWeather.get(p).daily_forecast.get(i).wind.sc};
                    daily.astros = new String[] {
                            result.heWeather.get(p).daily_forecast.get(i).astro.sr,
                            result.heWeather.get(p).daily_forecast.get(i).astro.ss};

                    w.dailyList.add(daily);
                } else {
                    Daily daily = new Daily();
                    daily.date = "null";
                    daily.week = "null";
                    daily.weathers = new String[] {"null", "null"};
                    daily.temps = new int[] {0, 0};
                    daily.windDirs = new String[] {"null", "null"};
                    daily.windLevels = new String[] {"null", "null"};
                    daily.astros = new String[] {"null", "null"};

                    w.dailyList.add(daily);
                }
            }

            w.hourlyList = new ArrayList<>();
            for (int i = 0; i < DAILY_LENGTH; i ++) {
                if (i < result.heWeather.get(p).hourly_forecast.size()) {
                    Hourly hourly = new Hourly();
                    hourly.hour = result.heWeather.get(p).hourly_forecast.get(i).date.split(" ")[1].split(":")[0];
                    hourly.temp = Integer.parseInt(result.heWeather.get(p).hourly_forecast.get(i).tmp);
                    hourly.pop = Integer.parseInt(result.heWeather.get(p).hourly_forecast.get(i).pop);

                    w.hourlyList.add(hourly);
                } else {
                    Hourly hourly = new Hourly();
                    hourly.hour = "null";
                    hourly.temp = 0;
                    hourly.pop = 0;

                    w.hourlyList.add(hourly);
                }
            }

            w.life = new Life();
            if (result.heWeather.get(p).suggestion == null) {
                w.life.winds = new String[] {
                        c.getString(R.string.live) + " - " + w.live.windDir + w.live.windLevel,
                        c.getString(R.string.today)
                                + result.heWeather.get(p).daily_forecast.get(0).wind.dir
                                + result.heWeather.get(p).daily_forecast.get(0).wind.sc
                                + "。"};
                w.life.pms = new String[] {
                        c.getString(R.string.visibility),
                        result.heWeather.get(p).now.vis + "km",};
                w.life.hums = new String[] {
                        c.getString(R.string.humidity),
                        result.heWeather.get(p).now.hum};
                w.life.uvs = new String[] {
                        c.getString(R.string.sun_rise) + "-" + result.heWeather.get(p).daily_forecast.get(0).astro.sr,
                        c.getString(R.string.sun_fall) + "-" + result.heWeather.get(p).daily_forecast.get(0).astro.ss};
                w.life.dresses = new String[] {
                        c.getString(R.string.apparent_temp),
                        result.heWeather.get(p).now.fl + "℃"};
                w.life.colds = new String[] {"", ""};
                w.life.airs = new String[] {"", ""};
                w.life.washCars = new String[] {"", ""};
                w.life.sports = new String[] {"", ""};
            } else {
                // wind.
                w.life.winds = new String[] {
                        c.getString(R.string.live) + " - " + w.live.windDir + w.live.windLevel,
                        c.getString(R.string.today)
                                + result.heWeather.get(p).daily_forecast.get(0).wind.dir
                                + result.heWeather.get(p).daily_forecast.get(0).wind.sc
                                + c.getString(R.string.level)
                                + "。"};
                // pm 2.5 & pm 10.
                if (result.heWeather.get(p).aqi != null) {
                    w.life.pms = new String[] {
                            c.getString(R.string.pm_25) + " - " + result.heWeather.get(p).aqi.city.pm25
                                    + " / "
                                    + c.getString(R.string.pm_10) + " - " + result.heWeather.get(p).aqi.city.pm10,
                            result.heWeather.get(p).aqi.city.qlty};
                } else {
                    w.life.pms = new String[] {"", ""};
                }
                // humidity.
                w.life.hums = new String[] {
                        c.getString(R.string.humidity),
                        result.heWeather.get(p).now.hum};
                // uv.
                w.life.uvs = new String[] {
                        c.getString(R.string.uv) + " - " + result.heWeather.get(p).suggestion.uv.brf,
                        result.heWeather.get(p).suggestion.uv.txt};
                // dress index.
                w.life.dresses = new String[] {
                        c.getString(R.string.dressing_index) + " - " + result.heWeather.get(p).suggestion.drsg.brf,
                        result.heWeather.get(p).suggestion.drsg.txt};
                // cold index.
                w.life.colds = new String[] {
                        c.getString(R.string.cold_index) + " - " + result.heWeather.get(p).suggestion.flu.brf,
                        result.heWeather.get(p).suggestion.flu.txt};
                // air index.
                w.life.airs = new String[] {"", ""};
                // wash car index.
                w.life.washCars = new String[] {
                        c.getString(R.string.wash_car_index) + " - " + result.heWeather.get(p).suggestion.cw.brf,
                        result.heWeather.get(p).suggestion.cw.txt};
                // exercise index.
                w.life.sports = new String[] {
                        c.getString(R.string.exercise_index) + " - " + result.heWeather.get(p).suggestion.sport.brf,
                        result.heWeather.get(p).suggestion.sport.txt};
            }

            return w;
        } catch (NullPointerException e) {
            return null;
        }
    }
}

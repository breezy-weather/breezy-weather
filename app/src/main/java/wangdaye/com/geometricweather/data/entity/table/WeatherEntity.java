package wangdaye.com.geometricweather.data.entity.table;

import org.greenrobot.greendao.annotation.Entity;

import wangdaye.com.geometricweather.data.entity.model.Weather;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Weather entity.
 * */

@Entity
public class WeatherEntity {
    // data
    @Id(autoincrement = true)
    public Long id;

    // base.
    @Unique
    public String location;
    public String refreshTime;

    public String date;
    public String moon;
    public String week;

    // live.
    public String weatherNow;
    public String weatherKindNow;
    public int tempNow;
    public String airNow;

    public String windDirNow;
    public String windLevelNow;

    // daily.
    public String dates_1;
    public String dates_2;
    public String dates_3;
    public String dates_4;
    public String dates_5;

    public String weeks_1;
    public String weeks_2;
    public String weeks_3;
    public String weeks_4;
    public String weeks_5;

    public String dayWeathers_1;
    public String dayWeathers_2;
    public String dayWeathers_3;
    public String dayWeathers_4;
    public String dayWeathers_5;

    public String dayWeatherKinds_1;
    public String dayWeatherKinds_2;
    public String dayWeatherKinds_3;
    public String dayWeatherKinds_4;
    public String dayWeatherKinds_5;

    public int dayTemps_1;
    public int dayTemps_2;
    public int dayTemps_3;
    public int dayTemps_4;
    public int dayTemps_5;

    public String dayWindDirs_1;
    public String dayWindDirs_2;
    public String dayWindDirs_3;
    public String dayWindDirs_4;
    public String dayWindDirs_5;

    public String dayWindLevels_1;
    public String dayWindLevels_2;
    public String dayWindLevels_3;
    public String dayWindLevels_4;
    public String dayWindLevels_5;

    public String sunriseTimes_1;
    public String sunriseTimes_2;
    public String sunriseTimes_3;
    public String sunriseTimes_4;
    public String sunriseTimes_5;

    public String nightWeathers_1;
    public String nightWeathers_2;
    public String nightWeathers_3;
    public String nightWeathers_4;
    public String nightWeathers_5;

    public String nightWeatherKinds_1;
    public String nightWeatherKinds_2;
    public String nightWeatherKinds_3;
    public String nightWeatherKinds_4;
    public String nightWeatherKinds_5;

    public int nightTemps_1;
    public int nightTemps_2;
    public int nightTemps_3;
    public int nightTemps_4;
    public int nightTemps_5;

    public String nightWindDirs_1;
    public String nightWindDirs_2;
    public String nightWindDirs_3;
    public String nightWindDirs_4;
    public String nightWindDirs_5;

    public String nightWindLevels_1;
    public String nightWindLevels_2;
    public String nightWindLevels_3;
    public String nightWindLevels_4;
    public String nightWindLevels_5;

    public String sunsetTimes_1;
    public String sunsetTimes_2;
    public String sunsetTimes_3;
    public String sunsetTimes_4;
    public String sunsetTimes_5;

    // hourly.
    public String hours_1;
    public String hours_2;
    public String hours_3;
    public String hours_4;
    public String hours_5;
    public String hours_6;
    public String hours_7;
    public String hours_8;

    public int hourlyTemps_1;
    public int hourlyTemps_2;
    public int hourlyTemps_3;
    public int hourlyTemps_4;
    public int hourlyTemps_5;
    public int hourlyTemps_6;
    public int hourlyTemps_7;
    public int hourlyTemps_8;

    public float hourlyPops_1;
    public float hourlyPops_2;
    public float hourlyPops_3;
    public float hourlyPops_4;
    public float hourlyPops_5;
    public float hourlyPops_6;
    public float hourlyPops_7;
    public float hourlyPops_8;

    //life.
    public String winds_1;
    public String winds_2;
    public String pms_1;
    public String pms_2;
    public String hums_1;
    public String hums_2;
    public String uvs_1;
    public String uvs_2;
    public String dresses_1;
    public String dresses_2;
    public String colds_1;
    public String colds_2;
    public String airs_1;
    public String airs_2;
    public String washCars_1;
    public String washCars_2;
    public String sports_1;
    public String sports_2;

    @Generated(hash = 317377435)
    public WeatherEntity(Long id, String location, String refreshTime, String date, String moon,
            String week, String weatherNow, String weatherKindNow, int tempNow, String airNow,
            String windDirNow, String windLevelNow, String dates_1, String dates_2, String dates_3,
            String dates_4, String dates_5, String weeks_1, String weeks_2, String weeks_3,
            String weeks_4, String weeks_5, String dayWeathers_1, String dayWeathers_2,
            String dayWeathers_3, String dayWeathers_4, String dayWeathers_5, String dayWeatherKinds_1,
            String dayWeatherKinds_2, String dayWeatherKinds_3, String dayWeatherKinds_4,
            String dayWeatherKinds_5, int dayTemps_1, int dayTemps_2, int dayTemps_3, int dayTemps_4,
            int dayTemps_5, String dayWindDirs_1, String dayWindDirs_2, String dayWindDirs_3,
            String dayWindDirs_4, String dayWindDirs_5, String dayWindLevels_1, String dayWindLevels_2,
            String dayWindLevels_3, String dayWindLevels_4, String dayWindLevels_5,
            String sunriseTimes_1, String sunriseTimes_2, String sunriseTimes_3, String sunriseTimes_4,
            String sunriseTimes_5, String nightWeathers_1, String nightWeathers_2,
            String nightWeathers_3, String nightWeathers_4, String nightWeathers_5,
            String nightWeatherKinds_1, String nightWeatherKinds_2, String nightWeatherKinds_3,
            String nightWeatherKinds_4, String nightWeatherKinds_5, int nightTemps_1, int nightTemps_2,
            int nightTemps_3, int nightTemps_4, int nightTemps_5, String nightWindDirs_1,
            String nightWindDirs_2, String nightWindDirs_3, String nightWindDirs_4,
            String nightWindDirs_5, String nightWindLevels_1, String nightWindLevels_2,
            String nightWindLevels_3, String nightWindLevels_4, String nightWindLevels_5,
            String sunsetTimes_1, String sunsetTimes_2, String sunsetTimes_3, String sunsetTimes_4,
            String sunsetTimes_5, String hours_1, String hours_2, String hours_3, String hours_4,
            String hours_5, String hours_6, String hours_7, String hours_8, int hourlyTemps_1,
            int hourlyTemps_2, int hourlyTemps_3, int hourlyTemps_4, int hourlyTemps_5,
            int hourlyTemps_6, int hourlyTemps_7, int hourlyTemps_8, float hourlyPops_1,
            float hourlyPops_2, float hourlyPops_3, float hourlyPops_4, float hourlyPops_5,
            float hourlyPops_6, float hourlyPops_7, float hourlyPops_8, String winds_1, String winds_2,
            String pms_1, String pms_2, String hums_1, String hums_2, String uvs_1, String uvs_2,
            String dresses_1, String dresses_2, String colds_1, String colds_2, String airs_1,
            String airs_2, String washCars_1, String washCars_2, String sports_1, String sports_2) {
        this.id = id;
        this.location = location;
        this.refreshTime = refreshTime;
        this.date = date;
        this.moon = moon;
        this.week = week;
        this.weatherNow = weatherNow;
        this.weatherKindNow = weatherKindNow;
        this.tempNow = tempNow;
        this.airNow = airNow;
        this.windDirNow = windDirNow;
        this.windLevelNow = windLevelNow;
        this.dates_1 = dates_1;
        this.dates_2 = dates_2;
        this.dates_3 = dates_3;
        this.dates_4 = dates_4;
        this.dates_5 = dates_5;
        this.weeks_1 = weeks_1;
        this.weeks_2 = weeks_2;
        this.weeks_3 = weeks_3;
        this.weeks_4 = weeks_4;
        this.weeks_5 = weeks_5;
        this.dayWeathers_1 = dayWeathers_1;
        this.dayWeathers_2 = dayWeathers_2;
        this.dayWeathers_3 = dayWeathers_3;
        this.dayWeathers_4 = dayWeathers_4;
        this.dayWeathers_5 = dayWeathers_5;
        this.dayWeatherKinds_1 = dayWeatherKinds_1;
        this.dayWeatherKinds_2 = dayWeatherKinds_2;
        this.dayWeatherKinds_3 = dayWeatherKinds_3;
        this.dayWeatherKinds_4 = dayWeatherKinds_4;
        this.dayWeatherKinds_5 = dayWeatherKinds_5;
        this.dayTemps_1 = dayTemps_1;
        this.dayTemps_2 = dayTemps_2;
        this.dayTemps_3 = dayTemps_3;
        this.dayTemps_4 = dayTemps_4;
        this.dayTemps_5 = dayTemps_5;
        this.dayWindDirs_1 = dayWindDirs_1;
        this.dayWindDirs_2 = dayWindDirs_2;
        this.dayWindDirs_3 = dayWindDirs_3;
        this.dayWindDirs_4 = dayWindDirs_4;
        this.dayWindDirs_5 = dayWindDirs_5;
        this.dayWindLevels_1 = dayWindLevels_1;
        this.dayWindLevels_2 = dayWindLevels_2;
        this.dayWindLevels_3 = dayWindLevels_3;
        this.dayWindLevels_4 = dayWindLevels_4;
        this.dayWindLevels_5 = dayWindLevels_5;
        this.sunriseTimes_1 = sunriseTimes_1;
        this.sunriseTimes_2 = sunriseTimes_2;
        this.sunriseTimes_3 = sunriseTimes_3;
        this.sunriseTimes_4 = sunriseTimes_4;
        this.sunriseTimes_5 = sunriseTimes_5;
        this.nightWeathers_1 = nightWeathers_1;
        this.nightWeathers_2 = nightWeathers_2;
        this.nightWeathers_3 = nightWeathers_3;
        this.nightWeathers_4 = nightWeathers_4;
        this.nightWeathers_5 = nightWeathers_5;
        this.nightWeatherKinds_1 = nightWeatherKinds_1;
        this.nightWeatherKinds_2 = nightWeatherKinds_2;
        this.nightWeatherKinds_3 = nightWeatherKinds_3;
        this.nightWeatherKinds_4 = nightWeatherKinds_4;
        this.nightWeatherKinds_5 = nightWeatherKinds_5;
        this.nightTemps_1 = nightTemps_1;
        this.nightTemps_2 = nightTemps_2;
        this.nightTemps_3 = nightTemps_3;
        this.nightTemps_4 = nightTemps_4;
        this.nightTemps_5 = nightTemps_5;
        this.nightWindDirs_1 = nightWindDirs_1;
        this.nightWindDirs_2 = nightWindDirs_2;
        this.nightWindDirs_3 = nightWindDirs_3;
        this.nightWindDirs_4 = nightWindDirs_4;
        this.nightWindDirs_5 = nightWindDirs_5;
        this.nightWindLevels_1 = nightWindLevels_1;
        this.nightWindLevels_2 = nightWindLevels_2;
        this.nightWindLevels_3 = nightWindLevels_3;
        this.nightWindLevels_4 = nightWindLevels_4;
        this.nightWindLevels_5 = nightWindLevels_5;
        this.sunsetTimes_1 = sunsetTimes_1;
        this.sunsetTimes_2 = sunsetTimes_2;
        this.sunsetTimes_3 = sunsetTimes_3;
        this.sunsetTimes_4 = sunsetTimes_4;
        this.sunsetTimes_5 = sunsetTimes_5;
        this.hours_1 = hours_1;
        this.hours_2 = hours_2;
        this.hours_3 = hours_3;
        this.hours_4 = hours_4;
        this.hours_5 = hours_5;
        this.hours_6 = hours_6;
        this.hours_7 = hours_7;
        this.hours_8 = hours_8;
        this.hourlyTemps_1 = hourlyTemps_1;
        this.hourlyTemps_2 = hourlyTemps_2;
        this.hourlyTemps_3 = hourlyTemps_3;
        this.hourlyTemps_4 = hourlyTemps_4;
        this.hourlyTemps_5 = hourlyTemps_5;
        this.hourlyTemps_6 = hourlyTemps_6;
        this.hourlyTemps_7 = hourlyTemps_7;
        this.hourlyTemps_8 = hourlyTemps_8;
        this.hourlyPops_1 = hourlyPops_1;
        this.hourlyPops_2 = hourlyPops_2;
        this.hourlyPops_3 = hourlyPops_3;
        this.hourlyPops_4 = hourlyPops_4;
        this.hourlyPops_5 = hourlyPops_5;
        this.hourlyPops_6 = hourlyPops_6;
        this.hourlyPops_7 = hourlyPops_7;
        this.hourlyPops_8 = hourlyPops_8;
        this.winds_1 = winds_1;
        this.winds_2 = winds_2;
        this.pms_1 = pms_1;
        this.pms_2 = pms_2;
        this.hums_1 = hums_1;
        this.hums_2 = hums_2;
        this.uvs_1 = uvs_1;
        this.uvs_2 = uvs_2;
        this.dresses_1 = dresses_1;
        this.dresses_2 = dresses_2;
        this.colds_1 = colds_1;
        this.colds_2 = colds_2;
        this.airs_1 = airs_1;
        this.airs_2 = airs_2;
        this.washCars_1 = washCars_1;
        this.washCars_2 = washCars_2;
        this.sports_1 = sports_1;
        this.sports_2 = sports_2;
    }

    @Generated(hash = 1598697471)
    public WeatherEntity() {
    }

    /** <br> life cycle. */

    public static WeatherEntity build(Weather weather) {
        WeatherEntity entity = new WeatherEntity();

        // base.
        entity.location = weather.base.location;
        entity.refreshTime = weather.base.refreshTime;

        entity.date = weather.base.date;
        entity.moon = weather.base.moon;
        entity.week = weather.base.week;

        // live.
        entity.weatherNow = weather.live.weather;
        entity.weatherKindNow = weather.live.weatherKind;
        entity.tempNow = weather.live.temp;
        entity.airNow = weather.live.air;

        entity.windDirNow = weather.live.windDir;
        entity.windLevelNow = weather.live.windLevel;

        // daily.
        entity.dates_1 = weather.dailyList.get(0).date;
        entity.dates_2 = weather.dailyList.get(1).date;
        entity.dates_3 = weather.dailyList.get(2).date;
        entity.dates_4 = weather.dailyList.get(3).date;
        entity.dates_5 = weather.dailyList.get(4).date;

        entity.weeks_1 = weather.dailyList.get(0).week;
        entity.weeks_2 = weather.dailyList.get(1).week;
        entity.weeks_3 = weather.dailyList.get(2).week;
        entity.weeks_4 = weather.dailyList.get(3).week;
        entity.weeks_5 = weather.dailyList.get(4).week;

        entity.dayWeathers_1 = weather.dailyList.get(0).weathers[0];
        entity.dayWeathers_2 = weather.dailyList.get(1).weathers[0];
        entity.dayWeathers_3 = weather.dailyList.get(2).weathers[0];
        entity.dayWeathers_4 = weather.dailyList.get(3).weathers[0];
        entity.dayWeathers_5 = weather.dailyList.get(4).weathers[0];

        entity.dayWeatherKinds_1 = weather.dailyList.get(0).weatherKinds[0];
        entity.dayWeatherKinds_2 = weather.dailyList.get(1).weatherKinds[0];
        entity.dayWeatherKinds_3 = weather.dailyList.get(2).weatherKinds[0];
        entity.dayWeatherKinds_4 = weather.dailyList.get(3).weatherKinds[0];
        entity.dayWeatherKinds_5 = weather.dailyList.get(4).weatherKinds[0];


        entity.dayTemps_1 = weather.dailyList.get(0).temps[0];
        entity.dayTemps_2 = weather.dailyList.get(1).temps[0];
        entity.dayTemps_3 = weather.dailyList.get(2).temps[0];
        entity.dayTemps_4 = weather.dailyList.get(3).temps[0];
        entity.dayTemps_5 = weather.dailyList.get(4).temps[0];

        entity.dayWindDirs_1 = weather.dailyList.get(0).windDirs[0];
        entity.dayWindDirs_2 = weather.dailyList.get(1).windDirs[0];
        entity.dayWindDirs_3 = weather.dailyList.get(2).windDirs[0];
        entity.dayWindDirs_4 = weather.dailyList.get(3).windDirs[0];
        entity.dayWindDirs_5 = weather.dailyList.get(4).windDirs[0];

        entity.dayWindLevels_1 = weather.dailyList.get(0).windLevels[0];
        entity.dayWindLevels_2 = weather.dailyList.get(1).windLevels[0];
        entity.dayWindLevels_3 = weather.dailyList.get(2).windLevels[0];
        entity.dayWindLevels_4 = weather.dailyList.get(3).windLevels[0];
        entity.dayWindLevels_5 = weather.dailyList.get(4).windLevels[0];

        entity.sunriseTimes_1 = weather.dailyList.get(0).astros[0];
        entity.sunriseTimes_2 = weather.dailyList.get(1).astros[0];
        entity.sunriseTimes_3 = weather.dailyList.get(2).astros[0];
        entity.sunriseTimes_4 = weather.dailyList.get(3).astros[0];
        entity.sunriseTimes_5 = weather.dailyList.get(4).astros[0];

        entity.nightWeathers_1 = weather.dailyList.get(0).weathers[1];
        entity.nightWeathers_2 = weather.dailyList.get(1).weathers[1];
        entity.nightWeathers_3 = weather.dailyList.get(2).weathers[1];
        entity.nightWeathers_4 = weather.dailyList.get(3).weathers[1];
        entity.nightWeathers_5 = weather.dailyList.get(4).weathers[1];

        entity.nightWeatherKinds_1 = weather.dailyList.get(0).weatherKinds[1];
        entity.nightWeatherKinds_2 = weather.dailyList.get(1).weatherKinds[1];
        entity.nightWeatherKinds_3 = weather.dailyList.get(2).weatherKinds[1];
        entity.nightWeatherKinds_4 = weather.dailyList.get(3).weatherKinds[1];
        entity.nightWeatherKinds_5 = weather.dailyList.get(4).weatherKinds[1];

        entity.nightTemps_1 = weather.dailyList.get(0).temps[1];
        entity.nightTemps_2 = weather.dailyList.get(1).temps[1];
        entity.nightTemps_3 = weather.dailyList.get(2).temps[1];
        entity.nightTemps_4 = weather.dailyList.get(3).temps[1];
        entity.nightTemps_5 = weather.dailyList.get(4).temps[1];

        entity.nightWindDirs_1 = weather.dailyList.get(0).windDirs[1];
        entity.nightWindDirs_2 = weather.dailyList.get(1).windDirs[1];
        entity.nightWindDirs_3 = weather.dailyList.get(2).windDirs[1];
        entity.nightWindDirs_4 = weather.dailyList.get(3).windDirs[1];
        entity.nightWindDirs_5 = weather.dailyList.get(4).windDirs[1];

        entity.nightWindLevels_1 = weather.dailyList.get(0).windLevels[1];
        entity.nightWindLevels_2 = weather.dailyList.get(1).windLevels[1];
        entity.nightWindLevels_3 = weather.dailyList.get(2).windLevels[1];
        entity.nightWindLevels_4 = weather.dailyList.get(3).windLevels[1];
        entity.nightWindLevels_5 = weather.dailyList.get(4).windLevels[1];

        entity.sunsetTimes_1 = weather.dailyList.get(0).astros[1];
        entity.sunsetTimes_2 = weather.dailyList.get(1).astros[1];
        entity.sunsetTimes_3 = weather.dailyList.get(2).astros[1];
        entity.sunsetTimes_4 = weather.dailyList.get(3).astros[1];
        entity.sunsetTimes_5 = weather.dailyList.get(4).astros[1];

        // hourly.
        entity.hours_1 = 0 < weather.hourlyList.size() ? weather.hourlyList.get(0).hour : "null";
        entity.hours_2 = 1 < weather.hourlyList.size() ? weather.hourlyList.get(1).hour : "null";
        entity.hours_3 = 2 < weather.hourlyList.size() ? weather.hourlyList.get(2).hour : "null";
        entity.hours_4 = 3 < weather.hourlyList.size() ? weather.hourlyList.get(3).hour : "null";
        entity.hours_5 = 4 < weather.hourlyList.size() ? weather.hourlyList.get(4).hour : "null";
        entity.hours_6 = 5 < weather.hourlyList.size() ? weather.hourlyList.get(5).hour : "null";
        entity.hours_7 = 6 < weather.hourlyList.size() ? weather.hourlyList.get(6).hour : "null";
        entity.hours_8 = 7 < weather.hourlyList.size() ? weather.hourlyList.get(7).hour : "null";

        entity.hourlyTemps_1 = 0 < weather.hourlyList.size() ? weather.hourlyList.get(0).temp : 0;
        entity.hourlyTemps_2 = 1 < weather.hourlyList.size() ? weather.hourlyList.get(1).temp : 0;
        entity.hourlyTemps_3 = 2 < weather.hourlyList.size() ? weather.hourlyList.get(2).temp : 0;
        entity.hourlyTemps_4 = 3 < weather.hourlyList.size() ? weather.hourlyList.get(3).temp : 0;
        entity.hourlyTemps_5 = 4 < weather.hourlyList.size() ? weather.hourlyList.get(4).temp : 0;
        entity.hourlyTemps_6 = 5 < weather.hourlyList.size() ? weather.hourlyList.get(5).temp : 0;
        entity.hourlyTemps_7 = 6 < weather.hourlyList.size() ? weather.hourlyList.get(6).temp : 0;
        entity.hourlyTemps_8 = 7 < weather.hourlyList.size() ? weather.hourlyList.get(7).temp : 0;

        entity.hourlyPops_1 = 0 < weather.hourlyList.size() ? weather.hourlyList.get(0).pop : 0;
        entity.hourlyPops_1 = 1 < weather.hourlyList.size() ? weather.hourlyList.get(1).pop : 0;
        entity.hourlyPops_1 = 2 < weather.hourlyList.size() ? weather.hourlyList.get(2).pop : 0;
        entity.hourlyPops_1 = 3 < weather.hourlyList.size() ? weather.hourlyList.get(3).pop : 0;
        entity.hourlyPops_1 = 4 < weather.hourlyList.size() ? weather.hourlyList.get(4).pop : 0;
        entity.hourlyPops_1 = 5 < weather.hourlyList.size() ? weather.hourlyList.get(5).pop : 0;
        entity.hourlyPops_1 = 6 < weather.hourlyList.size() ? weather.hourlyList.get(6).pop : 0;
        entity.hourlyPops_1 = 7 < weather.hourlyList.size() ? weather.hourlyList.get(7).pop : 0;

        //life.
        entity.winds_1 = weather.life.winds[0];
        entity.winds_2 = weather.life.winds[1];
        entity.pms_1 = weather.life.pms[0];
        entity.pms_2 = weather.life.pms[1];
        entity.hums_1 = weather.life.hums[0];
        entity.hums_2 = weather.life.hums[1];
        entity.uvs_1 = weather.life.uvs[0];
        entity.uvs_2 = weather.life.uvs[1];
        entity.dresses_1 = weather.life.dresses[0];
        entity.dresses_2 = weather.life.dresses[1];
        entity.colds_1 = weather.life.colds[0];
        entity.colds_2 = weather.life.colds[1];
        entity.airs_1 = weather.life.airs[0];
        entity.airs_2 = weather.life.airs[1];
        entity.washCars_1 = weather.life.washCars[0];
        entity.washCars_2 = weather.life.washCars[1];
        entity.sports_1 = weather.life.sports[0];
        entity.sports_2 = weather.life.sports[1];

        return entity;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRefreshTime() {
        return this.refreshTime;
    }

    public void setRefreshTime(String refreshTime) {
        this.refreshTime = refreshTime;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMoon() {
        return this.moon;
    }

    public void setMoon(String moon) {
        this.moon = moon;
    }

    public String getWeek() {
        return this.week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWeatherNow() {
        return this.weatherNow;
    }

    public void setWeatherNow(String weatherNow) {
        this.weatherNow = weatherNow;
    }

    public String getWeatherKindNow() {
        return this.weatherKindNow;
    }

    public void setWeatherKindNow(String weatherKindNow) {
        this.weatherKindNow = weatherKindNow;
    }

    public int getTempNow() {
        return this.tempNow;
    }

    public void setTempNow(int tempNow) {
        this.tempNow = tempNow;
    }

    public String getAirNow() {
        return this.airNow;
    }

    public void setAirNow(String airNow) {
        this.airNow = airNow;
    }

    public String getWindDirNow() {
        return this.windDirNow;
    }

    public void setWindDirNow(String windDirNow) {
        this.windDirNow = windDirNow;
    }

    public String getWindLevelNow() {
        return this.windLevelNow;
    }

    public void setWindLevelNow(String windLevelNow) {
        this.windLevelNow = windLevelNow;
    }

    public String getDates_1() {
        return this.dates_1;
    }

    public void setDates_1(String dates_1) {
        this.dates_1 = dates_1;
    }

    public String getDates_2() {
        return this.dates_2;
    }

    public void setDates_2(String dates_2) {
        this.dates_2 = dates_2;
    }

    public String getDates_3() {
        return this.dates_3;
    }

    public void setDates_3(String dates_3) {
        this.dates_3 = dates_3;
    }

    public String getDates_4() {
        return this.dates_4;
    }

    public void setDates_4(String dates_4) {
        this.dates_4 = dates_4;
    }

    public String getDates_5() {
        return this.dates_5;
    }

    public void setDates_5(String dates_5) {
        this.dates_5 = dates_5;
    }

    public String getWeeks_1() {
        return this.weeks_1;
    }

    public void setWeeks_1(String weeks_1) {
        this.weeks_1 = weeks_1;
    }

    public String getWeeks_2() {
        return this.weeks_2;
    }

    public void setWeeks_2(String weeks_2) {
        this.weeks_2 = weeks_2;
    }

    public String getWeeks_3() {
        return this.weeks_3;
    }

    public void setWeeks_3(String weeks_3) {
        this.weeks_3 = weeks_3;
    }

    public String getWeeks_4() {
        return this.weeks_4;
    }

    public void setWeeks_4(String weeks_4) {
        this.weeks_4 = weeks_4;
    }

    public String getWeeks_5() {
        return this.weeks_5;
    }

    public void setWeeks_5(String weeks_5) {
        this.weeks_5 = weeks_5;
    }

    public String getDayWeathers_1() {
        return this.dayWeathers_1;
    }

    public void setDayWeathers_1(String dayWeathers_1) {
        this.dayWeathers_1 = dayWeathers_1;
    }

    public String getDayWeathers_2() {
        return this.dayWeathers_2;
    }

    public void setDayWeathers_2(String dayWeathers_2) {
        this.dayWeathers_2 = dayWeathers_2;
    }

    public String getDayWeathers_3() {
        return this.dayWeathers_3;
    }

    public void setDayWeathers_3(String dayWeathers_3) {
        this.dayWeathers_3 = dayWeathers_3;
    }

    public String getDayWeathers_4() {
        return this.dayWeathers_4;
    }

    public void setDayWeathers_4(String dayWeathers_4) {
        this.dayWeathers_4 = dayWeathers_4;
    }

    public String getDayWeathers_5() {
        return this.dayWeathers_5;
    }

    public void setDayWeathers_5(String dayWeathers_5) {
        this.dayWeathers_5 = dayWeathers_5;
    }

    public String getDayWeatherKinds_1() {
        return this.dayWeatherKinds_1;
    }

    public void setDayWeatherKinds_1(String dayWeatherKinds_1) {
        this.dayWeatherKinds_1 = dayWeatherKinds_1;
    }

    public String getDayWeatherKinds_2() {
        return this.dayWeatherKinds_2;
    }

    public void setDayWeatherKinds_2(String dayWeatherKinds_2) {
        this.dayWeatherKinds_2 = dayWeatherKinds_2;
    }

    public String getDayWeatherKinds_3() {
        return this.dayWeatherKinds_3;
    }

    public void setDayWeatherKinds_3(String dayWeatherKinds_3) {
        this.dayWeatherKinds_3 = dayWeatherKinds_3;
    }

    public String getDayWeatherKinds_4() {
        return this.dayWeatherKinds_4;
    }

    public void setDayWeatherKinds_4(String dayWeatherKinds_4) {
        this.dayWeatherKinds_4 = dayWeatherKinds_4;
    }

    public String getDayWeatherKinds_5() {
        return this.dayWeatherKinds_5;
    }

    public void setDayWeatherKinds_5(String dayWeatherKinds_5) {
        this.dayWeatherKinds_5 = dayWeatherKinds_5;
    }

    public int getDayTemps_1() {
        return this.dayTemps_1;
    }

    public void setDayTemps_1(int dayTemps_1) {
        this.dayTemps_1 = dayTemps_1;
    }

    public int getDayTemps_2() {
        return this.dayTemps_2;
    }

    public void setDayTemps_2(int dayTemps_2) {
        this.dayTemps_2 = dayTemps_2;
    }

    public int getDayTemps_3() {
        return this.dayTemps_3;
    }

    public void setDayTemps_3(int dayTemps_3) {
        this.dayTemps_3 = dayTemps_3;
    }

    public int getDayTemps_4() {
        return this.dayTemps_4;
    }

    public void setDayTemps_4(int dayTemps_4) {
        this.dayTemps_4 = dayTemps_4;
    }

    public int getDayTemps_5() {
        return this.dayTemps_5;
    }

    public void setDayTemps_5(int dayTemps_5) {
        this.dayTemps_5 = dayTemps_5;
    }

    public String getDayWindDirs_1() {
        return this.dayWindDirs_1;
    }

    public void setDayWindDirs_1(String dayWindDirs_1) {
        this.dayWindDirs_1 = dayWindDirs_1;
    }

    public String getDayWindDirs_2() {
        return this.dayWindDirs_2;
    }

    public void setDayWindDirs_2(String dayWindDirs_2) {
        this.dayWindDirs_2 = dayWindDirs_2;
    }

    public String getDayWindDirs_3() {
        return this.dayWindDirs_3;
    }

    public void setDayWindDirs_3(String dayWindDirs_3) {
        this.dayWindDirs_3 = dayWindDirs_3;
    }

    public String getDayWindDirs_4() {
        return this.dayWindDirs_4;
    }

    public void setDayWindDirs_4(String dayWindDirs_4) {
        this.dayWindDirs_4 = dayWindDirs_4;
    }

    public String getDayWindDirs_5() {
        return this.dayWindDirs_5;
    }

    public void setDayWindDirs_5(String dayWindDirs_5) {
        this.dayWindDirs_5 = dayWindDirs_5;
    }

    public String getDayWindLevels_1() {
        return this.dayWindLevels_1;
    }

    public void setDayWindLevels_1(String dayWindLevels_1) {
        this.dayWindLevels_1 = dayWindLevels_1;
    }

    public String getDayWindLevels_2() {
        return this.dayWindLevels_2;
    }

    public void setDayWindLevels_2(String dayWindLevels_2) {
        this.dayWindLevels_2 = dayWindLevels_2;
    }

    public String getDayWindLevels_3() {
        return this.dayWindLevels_3;
    }

    public void setDayWindLevels_3(String dayWindLevels_3) {
        this.dayWindLevels_3 = dayWindLevels_3;
    }

    public String getDayWindLevels_4() {
        return this.dayWindLevels_4;
    }

    public void setDayWindLevels_4(String dayWindLevels_4) {
        this.dayWindLevels_4 = dayWindLevels_4;
    }

    public String getDayWindLevels_5() {
        return this.dayWindLevels_5;
    }

    public void setDayWindLevels_5(String dayWindLevels_5) {
        this.dayWindLevels_5 = dayWindLevels_5;
    }

    public String getSunriseTimes_1() {
        return this.sunriseTimes_1;
    }

    public void setSunriseTimes_1(String sunriseTimes_1) {
        this.sunriseTimes_1 = sunriseTimes_1;
    }

    public String getSunriseTimes_2() {
        return this.sunriseTimes_2;
    }

    public void setSunriseTimes_2(String sunriseTimes_2) {
        this.sunriseTimes_2 = sunriseTimes_2;
    }

    public String getSunriseTimes_3() {
        return this.sunriseTimes_3;
    }

    public void setSunriseTimes_3(String sunriseTimes_3) {
        this.sunriseTimes_3 = sunriseTimes_3;
    }

    public String getSunriseTimes_4() {
        return this.sunriseTimes_4;
    }

    public void setSunriseTimes_4(String sunriseTimes_4) {
        this.sunriseTimes_4 = sunriseTimes_4;
    }

    public String getSunriseTimes_5() {
        return this.sunriseTimes_5;
    }

    public void setSunriseTimes_5(String sunriseTimes_5) {
        this.sunriseTimes_5 = sunriseTimes_5;
    }

    public String getNightWeathers_1() {
        return this.nightWeathers_1;
    }

    public void setNightWeathers_1(String nightWeathers_1) {
        this.nightWeathers_1 = nightWeathers_1;
    }

    public String getNightWeathers_2() {
        return this.nightWeathers_2;
    }

    public void setNightWeathers_2(String nightWeathers_2) {
        this.nightWeathers_2 = nightWeathers_2;
    }

    public String getNightWeathers_3() {
        return this.nightWeathers_3;
    }

    public void setNightWeathers_3(String nightWeathers_3) {
        this.nightWeathers_3 = nightWeathers_3;
    }

    public String getNightWeathers_4() {
        return this.nightWeathers_4;
    }

    public void setNightWeathers_4(String nightWeathers_4) {
        this.nightWeathers_4 = nightWeathers_4;
    }

    public String getNightWeathers_5() {
        return this.nightWeathers_5;
    }

    public void setNightWeathers_5(String nightWeathers_5) {
        this.nightWeathers_5 = nightWeathers_5;
    }

    public String getNightWeatherKinds_1() {
        return this.nightWeatherKinds_1;
    }

    public void setNightWeatherKinds_1(String nightWeatherKinds_1) {
        this.nightWeatherKinds_1 = nightWeatherKinds_1;
    }

    public String getNightWeatherKinds_2() {
        return this.nightWeatherKinds_2;
    }

    public void setNightWeatherKinds_2(String nightWeatherKinds_2) {
        this.nightWeatherKinds_2 = nightWeatherKinds_2;
    }

    public String getNightWeatherKinds_3() {
        return this.nightWeatherKinds_3;
    }

    public void setNightWeatherKinds_3(String nightWeatherKinds_3) {
        this.nightWeatherKinds_3 = nightWeatherKinds_3;
    }

    public String getNightWeatherKinds_4() {
        return this.nightWeatherKinds_4;
    }

    public void setNightWeatherKinds_4(String nightWeatherKinds_4) {
        this.nightWeatherKinds_4 = nightWeatherKinds_4;
    }

    public String getNightWeatherKinds_5() {
        return this.nightWeatherKinds_5;
    }

    public void setNightWeatherKinds_5(String nightWeatherKinds_5) {
        this.nightWeatherKinds_5 = nightWeatherKinds_5;
    }

    public int getNightTemps_1() {
        return this.nightTemps_1;
    }

    public void setNightTemps_1(int nightTemps_1) {
        this.nightTemps_1 = nightTemps_1;
    }

    public int getNightTemps_2() {
        return this.nightTemps_2;
    }

    public void setNightTemps_2(int nightTemps_2) {
        this.nightTemps_2 = nightTemps_2;
    }

    public int getNightTemps_3() {
        return this.nightTemps_3;
    }

    public void setNightTemps_3(int nightTemps_3) {
        this.nightTemps_3 = nightTemps_3;
    }

    public int getNightTemps_4() {
        return this.nightTemps_4;
    }

    public void setNightTemps_4(int nightTemps_4) {
        this.nightTemps_4 = nightTemps_4;
    }

    public int getNightTemps_5() {
        return this.nightTemps_5;
    }

    public void setNightTemps_5(int nightTemps_5) {
        this.nightTemps_5 = nightTemps_5;
    }

    public String getNightWindDirs_1() {
        return this.nightWindDirs_1;
    }

    public void setNightWindDirs_1(String nightWindDirs_1) {
        this.nightWindDirs_1 = nightWindDirs_1;
    }

    public String getNightWindDirs_2() {
        return this.nightWindDirs_2;
    }

    public void setNightWindDirs_2(String nightWindDirs_2) {
        this.nightWindDirs_2 = nightWindDirs_2;
    }

    public String getNightWindDirs_3() {
        return this.nightWindDirs_3;
    }

    public void setNightWindDirs_3(String nightWindDirs_3) {
        this.nightWindDirs_3 = nightWindDirs_3;
    }

    public String getNightWindDirs_4() {
        return this.nightWindDirs_4;
    }

    public void setNightWindDirs_4(String nightWindDirs_4) {
        this.nightWindDirs_4 = nightWindDirs_4;
    }

    public String getNightWindDirs_5() {
        return this.nightWindDirs_5;
    }

    public void setNightWindDirs_5(String nightWindDirs_5) {
        this.nightWindDirs_5 = nightWindDirs_5;
    }

    public String getNightWindLevels_1() {
        return this.nightWindLevels_1;
    }

    public void setNightWindLevels_1(String nightWindLevels_1) {
        this.nightWindLevels_1 = nightWindLevels_1;
    }

    public String getNightWindLevels_2() {
        return this.nightWindLevels_2;
    }

    public void setNightWindLevels_2(String nightWindLevels_2) {
        this.nightWindLevels_2 = nightWindLevels_2;
    }

    public String getNightWindLevels_3() {
        return this.nightWindLevels_3;
    }

    public void setNightWindLevels_3(String nightWindLevels_3) {
        this.nightWindLevels_3 = nightWindLevels_3;
    }

    public String getNightWindLevels_4() {
        return this.nightWindLevels_4;
    }

    public void setNightWindLevels_4(String nightWindLevels_4) {
        this.nightWindLevels_4 = nightWindLevels_4;
    }

    public String getNightWindLevels_5() {
        return this.nightWindLevels_5;
    }

    public void setNightWindLevels_5(String nightWindLevels_5) {
        this.nightWindLevels_5 = nightWindLevels_5;
    }

    public String getSunsetTimes_1() {
        return this.sunsetTimes_1;
    }

    public void setSunsetTimes_1(String sunsetTimes_1) {
        this.sunsetTimes_1 = sunsetTimes_1;
    }

    public String getSunsetTimes_2() {
        return this.sunsetTimes_2;
    }

    public void setSunsetTimes_2(String sunsetTimes_2) {
        this.sunsetTimes_2 = sunsetTimes_2;
    }

    public String getSunsetTimes_3() {
        return this.sunsetTimes_3;
    }

    public void setSunsetTimes_3(String sunsetTimes_3) {
        this.sunsetTimes_3 = sunsetTimes_3;
    }

    public String getSunsetTimes_4() {
        return this.sunsetTimes_4;
    }

    public void setSunsetTimes_4(String sunsetTimes_4) {
        this.sunsetTimes_4 = sunsetTimes_4;
    }

    public String getSunsetTimes_5() {
        return this.sunsetTimes_5;
    }

    public void setSunsetTimes_5(String sunsetTimes_5) {
        this.sunsetTimes_5 = sunsetTimes_5;
    }

    public String getHours_1() {
        return this.hours_1;
    }

    public void setHours_1(String hours_1) {
        this.hours_1 = hours_1;
    }

    public String getHours_2() {
        return this.hours_2;
    }

    public void setHours_2(String hours_2) {
        this.hours_2 = hours_2;
    }

    public String getHours_3() {
        return this.hours_3;
    }

    public void setHours_3(String hours_3) {
        this.hours_3 = hours_3;
    }

    public String getHours_4() {
        return this.hours_4;
    }

    public void setHours_4(String hours_4) {
        this.hours_4 = hours_4;
    }

    public String getHours_5() {
        return this.hours_5;
    }

    public void setHours_5(String hours_5) {
        this.hours_5 = hours_5;
    }

    public String getHours_6() {
        return this.hours_6;
    }

    public void setHours_6(String hours_6) {
        this.hours_6 = hours_6;
    }

    public String getHours_7() {
        return this.hours_7;
    }

    public void setHours_7(String hours_7) {
        this.hours_7 = hours_7;
    }

    public String getHours_8() {
        return this.hours_8;
    }

    public void setHours_8(String hours_8) {
        this.hours_8 = hours_8;
    }

    public int getHourlyTemps_1() {
        return this.hourlyTemps_1;
    }

    public void setHourlyTemps_1(int hourlyTemps_1) {
        this.hourlyTemps_1 = hourlyTemps_1;
    }

    public int getHourlyTemps_2() {
        return this.hourlyTemps_2;
    }

    public void setHourlyTemps_2(int hourlyTemps_2) {
        this.hourlyTemps_2 = hourlyTemps_2;
    }

    public int getHourlyTemps_3() {
        return this.hourlyTemps_3;
    }

    public void setHourlyTemps_3(int hourlyTemps_3) {
        this.hourlyTemps_3 = hourlyTemps_3;
    }

    public int getHourlyTemps_4() {
        return this.hourlyTemps_4;
    }

    public void setHourlyTemps_4(int hourlyTemps_4) {
        this.hourlyTemps_4 = hourlyTemps_4;
    }

    public int getHourlyTemps_5() {
        return this.hourlyTemps_5;
    }

    public void setHourlyTemps_5(int hourlyTemps_5) {
        this.hourlyTemps_5 = hourlyTemps_5;
    }

    public int getHourlyTemps_6() {
        return this.hourlyTemps_6;
    }

    public void setHourlyTemps_6(int hourlyTemps_6) {
        this.hourlyTemps_6 = hourlyTemps_6;
    }

    public int getHourlyTemps_7() {
        return this.hourlyTemps_7;
    }

    public void setHourlyTemps_7(int hourlyTemps_7) {
        this.hourlyTemps_7 = hourlyTemps_7;
    }

    public int getHourlyTemps_8() {
        return this.hourlyTemps_8;
    }

    public void setHourlyTemps_8(int hourlyTemps_8) {
        this.hourlyTemps_8 = hourlyTemps_8;
    }

    public float getHourlyPops_1() {
        return this.hourlyPops_1;
    }

    public void setHourlyPops_1(float hourlyPops_1) {
        this.hourlyPops_1 = hourlyPops_1;
    }

    public float getHourlyPops_2() {
        return this.hourlyPops_2;
    }

    public void setHourlyPops_2(float hourlyPops_2) {
        this.hourlyPops_2 = hourlyPops_2;
    }

    public float getHourlyPops_3() {
        return this.hourlyPops_3;
    }

    public void setHourlyPops_3(float hourlyPops_3) {
        this.hourlyPops_3 = hourlyPops_3;
    }

    public float getHourlyPops_4() {
        return this.hourlyPops_4;
    }

    public void setHourlyPops_4(float hourlyPops_4) {
        this.hourlyPops_4 = hourlyPops_4;
    }

    public float getHourlyPops_5() {
        return this.hourlyPops_5;
    }

    public void setHourlyPops_5(float hourlyPops_5) {
        this.hourlyPops_5 = hourlyPops_5;
    }

    public float getHourlyPops_6() {
        return this.hourlyPops_6;
    }

    public void setHourlyPops_6(float hourlyPops_6) {
        this.hourlyPops_6 = hourlyPops_6;
    }

    public float getHourlyPops_7() {
        return this.hourlyPops_7;
    }

    public void setHourlyPops_7(float hourlyPops_7) {
        this.hourlyPops_7 = hourlyPops_7;
    }

    public float getHourlyPops_8() {
        return this.hourlyPops_8;
    }

    public void setHourlyPops_8(float hourlyPops_8) {
        this.hourlyPops_8 = hourlyPops_8;
    }

    public String getWinds_1() {
        return this.winds_1;
    }

    public void setWinds_1(String winds_1) {
        this.winds_1 = winds_1;
    }

    public String getWinds_2() {
        return this.winds_2;
    }

    public void setWinds_2(String winds_2) {
        this.winds_2 = winds_2;
    }

    public String getPms_1() {
        return this.pms_1;
    }

    public void setPms_1(String pms_1) {
        this.pms_1 = pms_1;
    }

    public String getPms_2() {
        return this.pms_2;
    }

    public void setPms_2(String pms_2) {
        this.pms_2 = pms_2;
    }

    public String getHums_1() {
        return this.hums_1;
    }

    public void setHums_1(String hums_1) {
        this.hums_1 = hums_1;
    }

    public String getHums_2() {
        return this.hums_2;
    }

    public void setHums_2(String hums_2) {
        this.hums_2 = hums_2;
    }

    public String getUvs_1() {
        return this.uvs_1;
    }

    public void setUvs_1(String uvs_1) {
        this.uvs_1 = uvs_1;
    }

    public String getUvs_2() {
        return this.uvs_2;
    }

    public void setUvs_2(String uvs_2) {
        this.uvs_2 = uvs_2;
    }

    public String getDresses_1() {
        return this.dresses_1;
    }

    public void setDresses_1(String dresses_1) {
        this.dresses_1 = dresses_1;
    }

    public String getDresses_2() {
        return this.dresses_2;
    }

    public void setDresses_2(String dresses_2) {
        this.dresses_2 = dresses_2;
    }

    public String getColds_1() {
        return this.colds_1;
    }

    public void setColds_1(String colds_1) {
        this.colds_1 = colds_1;
    }

    public String getColds_2() {
        return this.colds_2;
    }

    public void setColds_2(String colds_2) {
        this.colds_2 = colds_2;
    }

    public String getAirs_1() {
        return this.airs_1;
    }

    public void setAirs_1(String airs_1) {
        this.airs_1 = airs_1;
    }

    public String getAirs_2() {
        return this.airs_2;
    }

    public void setAirs_2(String airs_2) {
        this.airs_2 = airs_2;
    }

    public String getWashCars_1() {
        return this.washCars_1;
    }

    public void setWashCars_1(String washCars_1) {
        this.washCars_1 = washCars_1;
    }

    public String getWashCars_2() {
        return this.washCars_2;
    }

    public void setWashCars_2(String washCars_2) {
        this.washCars_2 = washCars_2;
    }

    public String getSports_1() {
        return this.sports_1;
    }

    public void setSports_1(String sports_1) {
        this.sports_1 = sports_1;
    }

    public String getSports_2() {
        return this.sports_2;
    }

    public void setSports_2(String sports_2) {
        this.sports_2 = sports_2;
    }
}

package wangdaye.com.geometricweather.data.entity.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.data.entity.result.FWResult;
import wangdaye.com.geometricweather.data.entity.result.HefengResult;
import wangdaye.com.geometricweather.data.entity.table.weather.AlarmEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.HourlyEntity;
import wangdaye.com.geometricweather.data.entity.table.weather.WeatherEntity;
import wangdaye.com.geometricweather.data.service.HefengWeather;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather
 * */

public class Weather {
    // data
    public Base base;
    public RealTime realTime;
    public Aqi aqi;
    public List<Daily> dailyList;
    public List<Hourly> hourlyList;
    public Life life;
    public List<Alarm> alarmList;

    /** <br> inner class. */

    public static class Base {
        public String cityId;
        public String city;
        public String date;
        public String time;
    }

    public static class RealTime {
        public String weather;
        public String weatherKind;
        public int temp;
        public int sendibleTemp;
        public String windDir;
        public String windLevel;
    }

    public static class Aqi {
        public String aqi;
        public String rank;
        public String pm25;
        public String pm10;
        public String quality;
        public String description;
    }

    public static class Daily {
        public String date;
        public String week;
        public String[] weathers;
        public String[] weatherKinds;
        public int[] temps;
        public String windDir;
        public String windLevel;
        public String[] astros;

        public Daily() {
            this.weathers = new String[] {"", ""};
            this.weatherKinds = new String[] {"", ""};
            this.temps = new int[] {0, 0};
            this.astros = new String[] {"", ""};
        }
    }

    public static class Hourly {
        public String time;
        public String weather;
        public String weatherKind;
        public int temp;
        public int precipitation;
    }

    public static class Life {
        public String[] winds;
        public String[] aqis;
        public String[] humidities;
        public String[] uvs;
        public String[] dresses;
        public String[] exercises;
        public String[] washCars;
        public String[] colds;

        public Life() {
            this.winds = new String[] {"", ""};
            this.aqis = new String[] {"", ""};
            this.humidities = new String[] {"", ""};
            this.uvs = new String[] {"", ""};
            this.dresses = new String[] {"", ""};
            this.exercises = new String[] {"", ""};
            this.washCars = new String[] {"", ""};
            this.colds = new String[] {"", ""};
        }
    }

    public static class Alarm implements Parcelable {
        public String content;
        public String description;
        public String name;
        public String level;
        public String color;
        public String typeCode;
        public String typeDescription;
        public String precaution;
        public String publishTime;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.content);
            dest.writeString(this.description);
            dest.writeString(this.name);
            dest.writeString(this.level);
            dest.writeString(this.color);
            dest.writeString(this.typeCode);
            dest.writeString(this.typeDescription);
            dest.writeString(this.precaution);
            dest.writeString(this.publishTime);
        }

        public Alarm() {
        }

        protected Alarm(Parcel in) {
            this.content = in.readString();
            this.description = in.readString();
            this.name = in.readString();
            this.level = in.readString();
            this.color = in.readString();
            this.typeCode = in.readString();
            this.typeDescription = in.readString();
            this.precaution = in.readString();
            this.publishTime = in.readString();
        }

        public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
            @Override
            public Alarm createFromParcel(Parcel source) {
                return new Alarm(source);
            }

            @Override
            public Alarm[] newArray(int size) {
                return new Alarm[size];
            }
        };
    }

    /** <br> life cycle. */

    private Weather() {
        this.base = new Base();
        this.realTime = new RealTime();
        this.aqi = new Aqi();
        this.dailyList = new ArrayList<>();
        this.hourlyList = new ArrayList<>();
        this.life = new Life();
        this.alarmList = new ArrayList<>();
    }

    public static Weather buildWeather(FWResult result) {
        if (result == null) {
            return null;
        }

        Weather weather = new Weather();

        weather.base.cityId = "CN" + result.cityid;
        weather.base.city = result.city;
        weather.base.date = result.realtime.time.split(" ")[0];
        weather.base.time = result.realtime.time.split(" ")[1].split(":")[0]
                + ":" + result.realtime.time.split(" ")[1].split(":")[1];

        weather.realTime.weather = result.realtime.weather;
        weather.realTime.weatherKind = WeatherHelper.getFWeatherKind(weather.realTime.weather);
        weather.realTime.temp = Integer.parseInt(result.realtime.temp.replace("°", ""));
        weather.realTime.sendibleTemp = Integer.parseInt(result.realtime.sendibleTemp.replace("°", ""));
        weather.realTime.windDir = result.realtime.wD;
        weather.realTime.windLevel = result.realtime.wS;

        weather.aqi.aqi = result.pm25.aqi;
        weather.aqi.rank = (int) (100.0 * result.pm25.cityrank / result.pm25.citycount) + "%";
        weather.aqi.pm25 = result.pm25.pm25;
        weather.aqi.pm10 = result.pm25.pm10;
        weather.aqi.quality = result.pm25.quality;
        weather.aqi.description = result.pm25.advice;

        for (int i = 0; i < result.weathers.size(); i ++) {
            Daily daily = new Daily();
            daily.date = result.weathers.get(i).date;
            daily.week = result.weathers.get(i).week.replace("星期", "周");
            daily.weathers = new String[] {
                    result.weathers.get(i).weather, result.weathers.get(i).weather};
            daily.weatherKinds = new String[] {
                    WeatherHelper.getFWeatherKind(daily.weathers[0]),
                    WeatherHelper.getFWeatherKind(daily.weathers[1])};
            daily.temps = new int[] {
                    Integer.parseInt(result.weathers.get(i).temp_day_c),
                    Integer.parseInt(result.weathers.get(i).temp_night_c)};
            daily.windDir = result.weathers.get(i).wd;
            daily.windLevel = result.weathers.get(i).ws;
            daily.astros = new String[] {
                    result.weathers.get(i).sun_rise_time,
                    result.weathers.get(i).sun_down_time};
            weather.dailyList.add(daily);
        }

        for (int i = 0; i < result.weatherDetailsInfo.weather24HoursDetailsInfos.size(); i ++) {
            Hourly hourly = new Hourly();
            hourly.time = result.weatherDetailsInfo.weather24HoursDetailsInfos.get(i)
                    .startTime.split(" ")[1].split(":")[0] + "时";
            hourly.weather = result.weatherDetailsInfo.weather24HoursDetailsInfos.get(i).weather;
            hourly.weatherKind = WeatherHelper.getFWeatherKind(hourly.weather);
            hourly.temp = Integer.parseInt(result.weatherDetailsInfo.weather24HoursDetailsInfos.get(i).highestTemperature);
            hourly.precipitation = Integer.parseInt(result.weatherDetailsInfo.weather24HoursDetailsInfos.get(i).precipitation);
            weather.hourlyList.add(hourly);
        }

        weather.life.winds = new String[] {
                "实时 - " + weather.realTime.windDir + " " + weather.realTime.windLevel,
                "今日 - " + weather.dailyList.get(0).windDir + " " + weather.dailyList.get(0).windLevel};
        weather.life.aqis = new String[] {
                "空气 - " + weather.aqi.quality + " (" + weather.aqi.aqi + ")",
                "PM2.5 - " + weather.aqi.pm25 + " / " + "PM10 - " + weather.aqi.pm10
                        + "\n" + result.indexes.get(10).content};
        weather.life.humidities = new String[] {
                "体感温度",
                weather.realTime.sendibleTemp +  "℃"};
        weather.life.uvs = new String[] {
                result.indexes.get(7).name + " - " + result.indexes.get(7).level,
                result.indexes.get(7).content};
        weather.life.dresses = new String[] {
                result.indexes.get(23).name + " - " + result.indexes.get(23).level,
                result.indexes.get(23).content};
        weather.life.exercises = new String[] {
                result.indexes.get(3).name + " - " + result.indexes.get(3).level,
                result.indexes.get(3).content};
        weather.life.washCars = new String[] {
                result.indexes.get(5).name + " - " + result.indexes.get(5).level,
                result.indexes.get(5).content};
        weather.life.colds = new String[] {
                result.indexes.get(19).name + " - " + result.indexes.get(19).level,
                result.indexes.get(19).content};

        for (int i = 0; i < result.alarms.size(); i ++) {
            Alarm alarm = new Alarm();
            alarm.content = result.alarms.get(i).alarmContent;
            alarm.description = result.alarms.get(i).alarmDesc;
            alarm.name = result.alarms.get(i).alarmId;
            alarm.level = result.alarms.get(i).alarmLevelNo;
            alarm.color = result.alarms.get(i).alarmLevelNoDesc;
            alarm.typeCode = result.alarms.get(i).alarmType;
            alarm.typeDescription = result.alarms.get(i).alarmTypeDesc;
            alarm.precaution = result.alarms.get(i).precaution;
            alarm.publishTime = result.alarms.get(i).publishTime;
            weather.alarmList.add(alarm);
        }

        return weather;
    }

    public static Weather buildWeather(HefengResult result) {
        int p = HefengWeather.getLatestDataPosition(result);
        if (result == null
                || result.heWeather == null || result.heWeather.size() == 0
                || !result.heWeather.get(p).status.equals("ok")) {
            return null;
        }

        try {
            Weather weather = new Weather();

            weather.base.cityId = result.heWeather.get(p).basic.id;
            weather.base.city = result.heWeather.get(p).basic.city;
            weather.base.time = result.heWeather.get(p).basic.update.loc.split(" ")[1];
            weather.base.date = result.heWeather.get(p).basic.update.loc.split(" ")[0];

            weather.realTime.weather = result.heWeather.get(p).now.cond.txt;
            weather.realTime.weatherKind = WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).now.cond.code);
            weather.realTime.temp = Integer.parseInt(result.heWeather.get(p).now.tmp);
            weather.realTime.sendibleTemp = Integer.parseInt(result.heWeather.get(p).now.fl);
            weather.realTime.windDir = result.heWeather.get(p).now.wind.dir;
            weather.realTime.windLevel = result.heWeather.get(p).now.wind.sc;

            for (int i = 0; i < result.heWeather.get(p).daily_forecast.size(); i ++) {
                Daily daily = new Daily();
                daily.date = result.heWeather.get(p).daily_forecast.get(i).date;
                daily.week = HefengWeather.getWeek(result.heWeather.get(p).daily_forecast.get(i).date);
                daily.weathers = new String[] {
                        result.heWeather.get(p).daily_forecast.get(i).cond.txt_d,
                        result.heWeather.get(p).daily_forecast.get(i).cond.txt_n};
                daily.weatherKinds = new String[] {
                        WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).daily_forecast.get(i).cond.code_d),
                        WeatherHelper.getHefengWeatherKind(result.heWeather.get(p).daily_forecast.get(i).cond.code_n)};
                daily.temps = new int[] {
                        Integer.parseInt(result.heWeather.get(p).daily_forecast.get(i).tmp.max),
                        Integer.parseInt(result.heWeather.get(p).daily_forecast.get(i).tmp.min)};
                daily.windDir = result.heWeather.get(p).daily_forecast.get(i).wind.dir;
                daily.windLevel = result.heWeather.get(p).daily_forecast.get(i).wind.sc;
                daily.astros = new String[] {
                        result.heWeather.get(p).daily_forecast.get(i).astro.sr,
                        result.heWeather.get(p).daily_forecast.get(i).astro.ss};
                weather.dailyList.add(daily);
            }

            for (int i = 0; i < result.heWeather.get(p).hourly_forecast.size(); i ++) {
                Hourly hourly = new Hourly();
                hourly.weather = "null";
                hourly.weatherKind = "null";
                hourly.time = result.heWeather.get(p).hourly_forecast.get(i).date.split(" ")[1].split(":")[0];
                hourly.temp = Integer.parseInt(result.heWeather.get(p).hourly_forecast.get(i).tmp);
                hourly.precipitation = Integer.parseInt(result.heWeather.get(p).hourly_forecast.get(i).pop);
                weather.hourlyList.add(hourly);
            }

            weather.life.winds = new String[] {
                    "Live - " + weather.realTime.windDir + weather.realTime.windLevel,
                    "Today - "
                            + result.heWeather.get(p).daily_forecast.get(0).wind.dir
                            + result.heWeather.get(p).daily_forecast.get(0).wind.sc
                            + "。"};
            weather.life.aqis = new String[] {
                    "Visibility",
                    result.heWeather.get(p).now.vis + "km",};
            weather.life.humidities = new String[] {
                    "Humidity",
                    result.heWeather.get(p).now.hum};
            weather.life.uvs = new String[] {
                    "Sunrise - " + result.heWeather.get(p).daily_forecast.get(0).astro.sr,
                    "Sunset - " + result.heWeather.get(p).daily_forecast.get(0).astro.ss};
            weather.life.dresses = new String[] {
                    "Apparent temperature",
                    result.heWeather.get(p).now.fl + "℃"};

            return weather;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Weather buildWeatherPrimaryData(WeatherEntity entity) {
        Weather weather = new Weather();

        // base.
        weather.base.cityId = entity.cityId;
        weather.base.city = entity.city;
        weather.base.date = entity.date;
        weather.base.time = entity.time;

        // realtime.
        weather.realTime.weather = entity.realTimeWeather;
        weather.realTime.weatherKind = entity.realTimeWeatherKind;
        weather.realTime.temp = entity.realTimeTemp;
        weather.realTime.sendibleTemp = entity.realTimeSendibleTemp;
        weather.realTime.windDir = entity.realTimeWindDir;
        weather.realTime.windLevel = entity.realTimeWindLevel;

        // aqi.
        weather.aqi.aqi = entity.aqiAqi;
        weather.aqi.rank = entity.aqiRank;
        weather.aqi.pm25 = entity.aqiPm25;
        weather.aqi.pm10 = entity.aqiPm10;
        weather.aqi.quality = entity.aqiQuality;
        weather.aqi.description = entity.aqiDescription;

        // life.
        assert weather.life.winds != null;
        weather.life.winds[0] = entity.lifeWindTitle;
        weather.life.winds[1] = entity.lifeWindContent;
        weather.life.aqis[0] = entity.lifeAqiTitle;
        weather.life.aqis[1] = entity.lifeAqiContent;
        weather.life.humidities[0] = entity.lifeHumidityTitle;
        weather.life.humidities[1] = entity.lifeHumidityContent;
        weather.life.uvs[0] = entity.lifeUvTitle;
        weather.life.uvs[1] = entity.lifeUvContent;
        weather.life.dresses[0] = entity.lifeDressesTitle;
        weather.life.dresses[1] = entity.lifeDressesContent;
        weather.life.exercises[0] = entity.lifeExerciseTitle;
        weather.life.exercises[1] = entity.lifeExerciseContent;
        weather.life.washCars[0] = entity.lifeWashCarTitle;
        weather.life.washCars[1] = entity.lifeWashCarContent;
        weather.life.colds[0] = entity.lifeColdTitle;
        weather.life.colds[1] = entity.lifeColdContent;

        return weather;
    }

    public Weather buildWeatherDailyList(List<DailyEntity> list) {
        dailyList = new ArrayList<>();
        for (int i = 0; i < list.size(); i ++) {
            Daily daily = new Daily();
            daily.date = list.get(i).date;
            daily.week = list.get(i).week;
            daily.weathers = new String[] {
                    list.get(i).daytimeWeather,
                    list.get(i).nighttimeWeather};
            daily.weatherKinds = new String[] {
                    list.get(i).daytimeWeatherKind,
                    list.get(i).nighttimeWeatherKind};
            daily.temps = new int[] {
                    list.get(i).maxiTemp,
                    list.get(i).miniTemp};
            daily.windDir = list.get(i).windDir;
            daily.windLevel = list.get(i).windLevel;
            daily.astros = new String[] {
                    list.get(i).sunrise,
                    list.get(i).sunset};
            dailyList.add(daily);
        }
        return this;
    }

    public Weather buildWeatherHourlyList(List<HourlyEntity> list) {
        hourlyList = new ArrayList<>();
        for (int i = 0; i < list.size(); i ++) {
            Hourly hourly = new Hourly();
            hourly.time = list.get(i).time;
            hourly.weather = list.get(i).weather;
            hourly.weatherKind = list.get(i).weatherKind;
            hourly.temp = list.get(i).temp;
            hourly.precipitation = list.get(i).precipitation;
            hourlyList.add(hourly);
        }
        return this;
    }

    public Weather buildWeatherAlarmList(List<AlarmEntity> list) {
        alarmList = new ArrayList<>();
        for (int i = 0; i < list.size(); i ++) {
            Alarm alarm = new Alarm();
            alarm.content = list.get(i).content;
            alarm.description = list.get(i).description;
            alarm.name = list.get(i).name;
            alarm.level = list.get(i).level;
            alarm.color = list.get(i).color;
            alarm.typeCode = list.get(i).typeCode;
            alarm.typeDescription = list.get(i).typeDescription;
            alarm.precaution = list.get(i).precaution;
            alarm.publishTime = list.get(i).publishTime;
            alarmList.add(alarm);
        }
        return this;
    }
}

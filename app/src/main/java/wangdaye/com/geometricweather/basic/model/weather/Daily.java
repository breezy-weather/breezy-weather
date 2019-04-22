package wangdaye.com.geometricweather.basic.model.weather;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import wangdaye.com.geometricweather.db.entity.DailyEntity;

/**
 * Daily.
 * */

public class Daily {

    public String date;
    public String week;
    public String[] weathers;
    public String[] weatherKinds;
    public int[] temps;
    public String[] windDirs;
    public String[] windSpeeds;
    public String[] windLevels;
    public int[] windDegrees;
    public String[] astros;
    public String moonPhase;
    public int[] precipitations;

    public Daily(String date, String week,
                 String[] weathers, String[] weatherKinds, int[] temps,
                 String[] windDirs, String[] windSpeeds, String[] windLevels, int[] windDegrees,
                 String[] astros, String moonPhase,
                 int[] precipitations) {
        this.date = date;
        this.week = week;
        this.weathers = weathers;
        this.weatherKinds = weatherKinds;
        this.temps = temps;
        this.windDirs = windDirs;
        this.windSpeeds = windSpeeds;
        this.windLevels = windLevels;
        this.windDegrees = windDegrees;
        this.astros = astros;
        this.moonPhase = moonPhase;
        this.precipitations = precipitations;
    }

    public DailyEntity toDailyEntity(Base base) {
        DailyEntity entity = new DailyEntity();
        entity.cityId = base.cityId;
        entity.city = base.city;
        entity.date = date;
        entity.week = week;
        entity.daytimeWeather = weathers[0];
        entity.nighttimeWeather = weathers[1];
        entity.daytimeWeatherKind = weatherKinds[0];
        entity.nighttimeWeatherKind = weatherKinds[1];
        entity.maxiTemp = temps[0];
        entity.miniTemp = temps[1];
        entity.daytimeWindDir = windDirs[0];
        entity.nighttimeWindDir = windDirs[1];
        entity.daytimeWindSpeed = windSpeeds[0];
        entity.nighttimeWindSpeed = windSpeeds[1];
        entity.daytimeWindLevel = windLevels[0];
        entity.nighttimeWindLevel = windLevels[1];
        entity.daytimeWindDegree = windDegrees[0];
        entity.nighttimeWindDegree = windDegrees[1];
        entity.sunrise = astros[0];
        entity.sunset = astros[1];
        entity.moonrise = astros[2];
        entity.moonset = astros[3];
        entity.moonPhase = moonPhase;
        entity.daytimePrecipitations = precipitations[0];
        entity.nighttimePrecipitations = precipitations[1];
        return entity;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDateInFormat(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                Calendar.YEAR,
                Integer.parseInt(date.split("-")[0])
        );
        calendar.set(
                Calendar.MONTH,
                Integer.parseInt(date.split("-")[1]) - 1
        );
        calendar.set(
                Calendar.DAY_OF_MONTH,
                Integer.parseInt(date.split("-")[2])
        );
        return new SimpleDateFormat(format).format(calendar.getTime());
    }
}

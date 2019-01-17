package wangdaye.com.geometricweather.data.entity.model.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import wangdaye.com.geometricweather.data.entity.result.accu.AccuDailyResult;
import wangdaye.com.geometricweather.data.entity.result.caiyun.CaiYunMainlyResult;
import wangdaye.com.geometricweather.data.entity.result.cn.CNWeatherResult;
import wangdaye.com.geometricweather.data.entity.table.weather.DailyEntity;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

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

    public Daily() {}

    public Daily buildDaily(Context c, AccuDailyResult.DailyForecasts forecast) {
        date = forecast.Date.split("T")[0];
        week = WeatherHelper.getWeek(c, date);
        weathers = new String[] {
                forecast.Day.IconPhrase,
                forecast.Night.IconPhrase};
        weatherKinds = new String[] {
                WeatherHelper.getAccuWeatherKind(forecast.Day.Icon),
                WeatherHelper.getAccuWeatherKind(forecast.Night.Icon)};
        temps = new int[] {
                (int) forecast.Temperature.Maximum.Value,
                (int) forecast.Temperature.Minimum.Value};
        windDirs = new String[] {
                forecast.Day.Wind.Direction.Localized,
                forecast.Night.Wind.Direction.Localized};
        windSpeeds = new String[] {
                WeatherHelper.getWindSpeed(forecast.Day.Wind.Speed.Value),
                WeatherHelper.getWindSpeed(forecast.Night.Wind.Speed.Value)};
        windLevels = new String[] {
                WeatherHelper.getWindLevel(c, forecast.Day.Wind.Speed.Value),
                WeatherHelper.getWindLevel(c, forecast.Night.Wind.Speed.Value)};
        windDegrees = new int[] {
                forecast.Day.Wind.Direction.Degrees,
                forecast.Night.Wind.Direction.Degrees};
        if (!TextUtils.isEmpty(forecast.Moon.Rise) && !TextUtils.isEmpty(forecast.Moon.Set)
                && !TextUtils.isEmpty(forecast.Moon.Rise) && !TextUtils.isEmpty(forecast.Moon.Set)) {
            astros = new String[] {
                    forecast.Sun.Rise.split("T")[1].split(":")[0]
                            + ":" + forecast.Sun.Rise.split("T")[1].split(":")[1],
                    forecast.Sun.Set.split("T")[1].split(":")[0]
                            + ":" + forecast.Sun.Set.split("T")[1].split(":")[1],
                    forecast.Moon.Rise.split("T")[1].split(":")[0]
                            + ":" + forecast.Moon.Rise.split("T")[1].split(":")[1],
                    forecast.Moon.Set.split("T")[1].split(":")[0]
                            + ":" + forecast.Moon.Set.split("T")[1].split(":")[1]};
        } else if (!TextUtils.isEmpty(forecast.Moon.Rise) && !TextUtils.isEmpty(forecast.Moon.Set)) {
            astros = new String[] {
                    forecast.Sun.Rise.split("T")[1].split(":")[0]
                            + ":" + forecast.Sun.Rise.split("T")[1].split(":")[1],
                    forecast.Sun.Set.split("T")[1].split(":")[0]
                            + ":" + forecast.Sun.Set.split("T")[1].split(":")[1],
                    "", ""};
        } else {
            astros = new String[] {"6:00", "18:00", "", ""};

        }
        moonPhase = forecast.Moon.Phase;
        precipitations = new int[] {forecast.Day.PrecipitationProbability, forecast.Night.PrecipitationProbability};
        return this;
    }

    public Daily buildDaily(Context c, CNWeatherResult.WeatherX daily) {
        date = daily.date;
        week = WeatherHelper.getWeek(c, date);
        weathers = new String[] {
                daily.info.day.get(1),
                daily.info.night.get(1)};
        weatherKinds = new String[] {
                WeatherHelper.getCNWeatherKind(daily.info.day.get(0)),
                WeatherHelper.getCNWeatherKind(daily.info.night.get(0))};
        temps = new int[] {
                Integer.parseInt(daily.info.day.get(2)),
                Integer.parseInt(daily.info.night.get(2))};
        windDirs = new String[] {
                daily.info.day.get(3),
                daily.info.night.get(3)};
        windSpeeds = new String[] {null, null};
        windLevels = new String[] {
                daily.info.day.get(4),
                daily.info.night.get(4)};
        windDegrees = new int[] {-1, -1};
        astros = new String[] {
                daily.info.day.get(5),
                daily.info.night.get(5),
                "", ""};
        moonPhase = "";
        precipitations = new int[] {-1, -1};
        return this;
    }

    public Daily buildDaily(Context c, CaiYunMainlyResult result, int index) {
        date = result.forecastDaily.sunRiseSet.value.get(index).from.split("T")[0];
        week = WeatherHelper.getWeek(c, date);
        weathers = new String[] {
                WeatherHelper.getCNWeatherName(result.forecastDaily.weather.value.get(index).from),
                WeatherHelper.getCNWeatherName(result.forecastDaily.weather.value.get(index).to)};
        weatherKinds = new String[] {
                WeatherHelper.getCNWeatherKind(result.forecastDaily.weather.value.get(index).from),
                WeatherHelper.getCNWeatherKind(result.forecastDaily.weather.value.get(index).to)};
        temps = new int[] {
                Integer.parseInt(result.forecastDaily.temperature.value.get(index).from),
                Integer.parseInt(result.forecastDaily.temperature.value.get(index).to)};
        windDegrees = new int[] {
                Integer.parseInt(result.forecastDaily.wind.direction.value.get(index).from),
                Integer.parseInt(result.forecastDaily.wind.direction.value.get(index).to)};
        windDirs = new String[] {
                WeatherHelper.getCNWindName(windDegrees[0]),
                WeatherHelper.getCNWindName(windDegrees[1])};
        windSpeeds = new String[] {
                result.forecastDaily.wind.speed.value.get(index).from,
                result.forecastDaily.wind.speed.value.get(index).to};
        try {
            windLevels = new String[] {
                    WeatherHelper.getWindLevel(c, Double.parseDouble(windSpeeds[0])),
                    WeatherHelper.getWindLevel(c, Double.parseDouble(windSpeeds[1]))};
        } catch (Exception e) {
            windLevels = new String[] {"", ""};
        }
        astros = new String[] {
                result.forecastDaily.sunRiseSet.value.get(index).from.split("T")[1].substring(0, 5),
                result.forecastDaily.sunRiseSet.value.get(index).to.split("T")[1].substring(0, 5),
                "", ""};
        moonPhase = "";
        if (index < result.forecastDaily.precipitationProbability.value.size()) {
            precipitations = new int[] {
                    Integer.parseInt(result.forecastDaily.precipitationProbability.value.get(index)),
                    Integer.parseInt(result.forecastDaily.precipitationProbability.value.get(index)),};
        } else {
            precipitations = new int[] {-1, -1};
        }
        return this;
    }

    Daily buildDaily(DailyEntity entity) {
        date = entity.date;
        week = entity.week;
        weathers = new String[] {
                entity.daytimeWeather,
                entity.nighttimeWeather};
        weatherKinds = new String[] {
                entity.daytimeWeatherKind,
                entity.nighttimeWeatherKind};
        temps = new int[] {
                entity.maxiTemp,
                entity.miniTemp};
        windDirs = new String[] {entity.daytimeWindDir, entity.nighttimeWindDir};
        try {
            windSpeeds = new String[] {
                    WeatherHelper.getWindSpeed(entity.daytimeWindSpeed),
                    WeatherHelper.getWindSpeed(entity.nighttimeWindSpeed)};
        } catch (Exception e) {
            windSpeeds = new String[] {entity.daytimeWindSpeed, entity.nighttimeWindSpeed};
        }
        windLevels = new String[] {entity.daytimeWindLevel, entity.nighttimeWindLevel};
        windDegrees = new int[] {entity.daytimeWindDegree, entity.nighttimeWindDegree};
        astros = new String[] {entity.sunrise, entity.sunset, entity.moonrise, entity.moonset};
        moonPhase = entity.moonPhase;
        precipitations = new int[] {entity.daytimePrecipitations, entity.nighttimePrecipitations};
        return this;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDateInFormat(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.split("-")[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.split("-")[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("-")[2]));
        return new SimpleDateFormat(format).format(calendar.getTime());
    }
}
